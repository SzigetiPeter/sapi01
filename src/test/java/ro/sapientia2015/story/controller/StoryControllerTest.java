package ro.sapientia2015.story.controller;

import static junit.framework.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.validation.support.BindingAwareModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import ro.sapientia2015.story.StoryTestUtil;
import ro.sapientia2015.story.config.UnitTestContext;
import ro.sapientia2015.story.dto.StoryDTO;
import ro.sapientia2015.story.exception.NotFoundException;
import ro.sapientia2015.story.model.Story;
import ro.sapientia2015.story.service.StoryService;

/**
 * @author Kiss Tibor
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {UnitTestContext.class})
public class StoryControllerTest {

    private static final String FEEDBACK_MESSAGE = "feedbackMessage";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_START_TIME = "startTime";
    private static final String FIELD_END_TIME = "endTime";

    private StoryController controller;

    private MessageSource messageSourceMock;

    private StoryService serviceMock;

    @Resource
    private Validator validator;

    @Before
    public void setUp() {
        controller = new StoryController();

        messageSourceMock = mock(MessageSource.class);
        ReflectionTestUtils.setField(controller, "messageSource", messageSourceMock);

        serviceMock = mock(StoryService.class);
        ReflectionTestUtils.setField(controller, "service", serviceMock);
    }

    @Test
    public void showAddStoryForm() {
        BindingAwareModelMap model = new BindingAwareModelMap();

        String view = controller.showAddForm(model);

        verifyZeroInteractions(messageSourceMock, serviceMock);
        assertEquals(StoryController.VIEW_ADD, view);

        StoryDTO formObject = (StoryDTO) model.asMap().get(StoryController.MODEL_ATTRIBUTE);

        assertNull(formObject.getId());
        assertNull(formObject.getDescription());
        assertNull(formObject.getTitle());
        assertNull(formObject.getStartTime());
        assertNull(formObject.getEndTime());
    }
  
    @Test
    public void add() throws ParseException {
        StoryDTO formObject = StoryTestUtil.createFormObject(null, StoryTestUtil.DESCRIPTION, StoryTestUtil.TITLE, StoryTestUtil.START_DATE, StoryTestUtil.END_DATE);

        Story model = StoryTestUtil.createModel(StoryTestUtil.ID, StoryTestUtil.DESCRIPTION, StoryTestUtil.TITLE, StoryTestUtil.START_DATE, StoryTestUtil.END_DATE);
        when(serviceMock.add(formObject)).thenReturn(model);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", "/story/add");
        BindingResult result = bindAndValidate(mockRequest, formObject);

        RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();

        initMessageSourceForFeedbackMessage(StoryController.FEEDBACK_MESSAGE_KEY_ADDED);

        String view = controller.add(formObject, result, attributes);

        verify(serviceMock, times(1)).add(formObject);
        verifyNoMoreInteractions(serviceMock);

        String expectedView = StoryTestUtil.createRedirectViewPath(StoryController.REQUEST_MAPPING_VIEW);
        assertEquals(expectedView, view);

        assertEquals(Long.valueOf((String) attributes.get(StoryController.PARAMETER_ID)), model.getId());

        assertFeedbackMessage(attributes, StoryController.FEEDBACK_MESSAGE_KEY_ADDED);
    }

    @Test
    public void addEmptyStory() throws ParseException {
        StoryDTO formObject = StoryTestUtil.createFormObject(null, "", "", "", "");

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", "/story/add");
        BindingResult result = bindAndValidate(mockRequest, formObject);

        RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();

        String view = controller.add(formObject, result, attributes);

        verifyZeroInteractions(serviceMock, messageSourceMock);

        assertEquals(StoryController.VIEW_ADD, view);
        assertFieldErrors(result, FIELD_TITLE, FIELD_START_TIME, FIELD_END_TIME);
    }

    @Test
    public void addWithTooLongDescriptionAndTitle() throws ParseException {
        String description = StoryTestUtil.createStringWithLength(Story.MAX_LENGTH_DESCRIPTION + 1);
        String title = StoryTestUtil.createStringWithLength(Story.MAX_LENGTH_TITLE + 1);

        StoryDTO formObject = StoryTestUtil.createFormObject(null, description, title, StoryTestUtil.START_DATE_UPDATED, StoryTestUtil.END_DATE_UPDATED);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", "/story/add");
        BindingResult result = bindAndValidate(mockRequest, formObject);

        RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();

        String view = controller.add(formObject, result, attributes);

        verifyZeroInteractions(serviceMock, messageSourceMock);

        assertEquals(StoryController.VIEW_ADD, view);
        assertFieldErrors(result, FIELD_DESCRIPTION, FIELD_TITLE);
    }

    @Test
    public void deleteById() throws NotFoundException {
        RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();

        Story model = StoryTestUtil.createModel(StoryTestUtil.ID, StoryTestUtil.DESCRIPTION, StoryTestUtil.TITLE);
        when(serviceMock.deleteById(StoryTestUtil.ID)).thenReturn(model);

        initMessageSourceForFeedbackMessage(StoryController.FEEDBACK_MESSAGE_KEY_DELETED);

        String view = controller.deleteById(StoryTestUtil.ID, attributes);

        verify(serviceMock, times(1)).deleteById(StoryTestUtil.ID);
        verifyNoMoreInteractions(serviceMock);

        assertFeedbackMessage(attributes, StoryController.FEEDBACK_MESSAGE_KEY_DELETED);

        String expectedView = StoryTestUtil.createRedirectViewPath(StoryController.REQUEST_MAPPING_LIST);
        assertEquals(expectedView, view);
    }

    @Test(expected = NotFoundException.class)
    public void deleteByIdWhenIsNotFound() throws NotFoundException {
        RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();

        when(serviceMock.deleteById(StoryTestUtil.ID)).thenThrow(new NotFoundException(""));

        controller.deleteById(StoryTestUtil.ID, attributes);

        verify(serviceMock, times(1)).deleteById(StoryTestUtil.ID);
        verifyNoMoreInteractions(serviceMock);
        verifyZeroInteractions(messageSourceMock);
    }

    @Test
    public void findAll() {
        BindingAwareModelMap model = new BindingAwareModelMap();

        List<Story> models = new ArrayList<Story>();
        when(serviceMock.findAll()).thenReturn(models);

        String view = controller.findAll(model);

        verify(serviceMock, times(1)).findAll();
        verifyNoMoreInteractions(serviceMock);
        verifyZeroInteractions(messageSourceMock);

        assertEquals(StoryController.VIEW_LIST, view);
        assertEquals(models, model.asMap().get(StoryController.MODEL_ATTRIBUTE_LIST));
    }

    @Test
    public void findById() throws NotFoundException {
        BindingAwareModelMap model = new BindingAwareModelMap();

        Story found = StoryTestUtil.createModel(StoryTestUtil.ID, StoryTestUtil.DESCRIPTION, StoryTestUtil.TITLE);
        when(serviceMock.findById(StoryTestUtil.ID)).thenReturn(found);

        String view = controller.findById(StoryTestUtil.ID, model);

        verify(serviceMock, times(1)).findById(StoryTestUtil.ID);
        verifyNoMoreInteractions(serviceMock);
        verifyZeroInteractions(messageSourceMock);

        assertEquals(StoryController.VIEW_VIEW, view);
        assertEquals(found, model.asMap().get(StoryController.MODEL_ATTRIBUTE));
    }

    @Test(expected = NotFoundException.class)
    public void findByIdWhenIsNotFound() throws NotFoundException {
        BindingAwareModelMap model = new BindingAwareModelMap();

        when(serviceMock.findById(StoryTestUtil.ID)).thenThrow(new NotFoundException(""));

        controller.findById(StoryTestUtil.ID, model);

        verify(serviceMock, times(1)).findById(StoryTestUtil.ID);
        verifyNoMoreInteractions(serviceMock);
        verifyZeroInteractions(messageSourceMock);
    }

    @Test
    public void showUpdateStoryForm() throws NotFoundException {
        BindingAwareModelMap model = new BindingAwareModelMap();

        Story updated = StoryTestUtil.createModel(StoryTestUtil.ID, StoryTestUtil.DESCRIPTION, StoryTestUtil.TITLE);
        when(serviceMock.findById(StoryTestUtil.ID)).thenReturn(updated);

        String view = controller.showUpdateForm(StoryTestUtil.ID, model);

        verify(serviceMock, times(1)).findById(StoryTestUtil.ID);
        verifyNoMoreInteractions(serviceMock);
        verifyZeroInteractions(messageSourceMock);

        assertEquals(StoryController.VIEW_UPDATE, view);

        StoryDTO formObject = (StoryDTO) model.asMap().get(StoryController.MODEL_ATTRIBUTE);

        assertEquals(updated.getId(), formObject.getId());
        assertEquals(updated.getDescription(), formObject.getDescription());
        assertEquals(updated.getTitle(), formObject.getTitle());
    }

    @Test(expected = NotFoundException.class)
    public void showUpdateStoryFormWhenIsNotFound() throws NotFoundException {
        BindingAwareModelMap model = new BindingAwareModelMap();

        when(serviceMock.findById(StoryTestUtil.ID)).thenThrow(new NotFoundException(""));

        controller.showUpdateForm(StoryTestUtil.ID, model);

        verify(serviceMock, times(1)).findById(StoryTestUtil.ID);
        verifyNoMoreInteractions(serviceMock);
        verifyZeroInteractions(messageSourceMock);
    }

    @Test
    public void update() throws NotFoundException, ParseException {
        StoryDTO formObject = StoryTestUtil.createFormObject(StoryTestUtil.ID, StoryTestUtil.DESCRIPTION_UPDATED, StoryTestUtil.TITLE_UPDATED, StoryTestUtil.START_DATE_UPDATED, StoryTestUtil.END_DATE_UPDATED);

        Story model = StoryTestUtil.createModel(StoryTestUtil.ID, StoryTestUtil.DESCRIPTION_UPDATED, StoryTestUtil.TITLE_UPDATED, StoryTestUtil.START_DATE_UPDATED, StoryTestUtil.END_DATE_UPDATED);
        when(serviceMock.update(formObject)).thenReturn(model);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", "/story/add");
        BindingResult result = bindAndValidate(mockRequest, formObject);

        RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();

        initMessageSourceForFeedbackMessage(StoryController.FEEDBACK_MESSAGE_KEY_UPDATED);

        String view = controller.update(formObject, result, attributes);

        verify(serviceMock, times(1)).update(formObject);
        verifyNoMoreInteractions(serviceMock);

        String expectedView = StoryTestUtil.createRedirectViewPath(StoryController.REQUEST_MAPPING_VIEW);
        assertEquals(expectedView, view);

        assertEquals(Long.valueOf((String) attributes.get(StoryController.PARAMETER_ID)), model.getId());

        assertFeedbackMessage(attributes, StoryController.FEEDBACK_MESSAGE_KEY_UPDATED);
    }

    @Test
    public void updateEmpty() throws NotFoundException, ParseException {
        StoryDTO formObject = StoryTestUtil.createFormObject(StoryTestUtil.ID, "", "", "", "");

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", "/story/add");
        BindingResult result = bindAndValidate(mockRequest, formObject);

        RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();

        String view = controller.update(formObject, result, attributes);

        verifyZeroInteractions(messageSourceMock, serviceMock);

        assertEquals(StoryController.VIEW_UPDATE, view);
        assertFieldErrors(result, FIELD_TITLE, FIELD_START_TIME, FIELD_END_TIME);
    }

    @Test
    public void updateWhenDescriptionAndTitleAreTooLong() throws NotFoundException, ParseException {
        String description = StoryTestUtil.createStringWithLength(Story.MAX_LENGTH_DESCRIPTION + 1);
        String title = StoryTestUtil.createStringWithLength(Story.MAX_LENGTH_TITLE + 1);

        StoryDTO formObject = StoryTestUtil.createFormObject(StoryTestUtil.ID, description, title, StoryTestUtil.START_DATE_UPDATED, StoryTestUtil.END_DATE_UPDATED);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", "/story/add");
        BindingResult result = bindAndValidate(mockRequest, formObject);

        RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();

        String view = controller.update(formObject, result, attributes);

        verifyZeroInteractions(messageSourceMock, serviceMock);

        assertEquals(StoryController.VIEW_UPDATE, view);
        assertFieldErrors(result, FIELD_DESCRIPTION, FIELD_TITLE);
    }

    @Test(expected = NotFoundException.class)
    public void updateWhenIsNotFound() throws NotFoundException, ParseException {
        StoryDTO formObject = StoryTestUtil.createFormObject(StoryTestUtil.ID, StoryTestUtil.DESCRIPTION_UPDATED, StoryTestUtil.TITLE_UPDATED, StoryTestUtil.START_DATE_UPDATED, StoryTestUtil.END_DATE_UPDATED);

        when(serviceMock.update(formObject)).thenThrow(new NotFoundException(""));

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", "/story/add");
        BindingResult result = bindAndValidate(mockRequest, formObject);

        RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();

        controller.update(formObject, result, attributes);

        verify(serviceMock, times(1)).update(formObject);
        verifyNoMoreInteractions(serviceMock);
        verifyZeroInteractions(messageSourceMock);
    }
    
    @Test
    public void addWithWrongDates() throws ParseException {
        String startDate = "2015-05-18 09:55";
        String endDate = "2015-05-08 09:55";

        StoryDTO formObject = StoryTestUtil.createFormObject(null, StoryTestUtil.TITLE, StoryTestUtil.DESCRIPTION, startDate, endDate);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", "/story/add");
        BindingResult result = bindAndValidate(mockRequest, formObject);

        RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();

        String view = controller.add(formObject, result, attributes);

        verifyZeroInteractions(serviceMock, messageSourceMock);

        assertEquals(StoryController.VIEW_ADD, view);
        assertFieldErrorsEmpty(result, FIELD_START_TIME, FIELD_END_TIME);
    }
    
    @Test(expected = ParseException.class)    
    public void addWithIncompleteDates() throws ParseException {
        String startDate = "2015-05 09:55";
        String endDate = "2015-05 09:55";

        StoryDTO formObject = StoryTestUtil.createFormObject(null, StoryTestUtil.TITLE, StoryTestUtil.DESCRIPTION, startDate, endDate);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", "/story/add");
        BindingResult result = bindAndValidate(mockRequest, formObject);

        RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();

        String view = controller.add(formObject, result, attributes);

        verifyZeroInteractions(serviceMock, messageSourceMock);

        assertEquals(StoryController.VIEW_ADD, view);
        assertFieldErrors(result, FIELD_START_TIME, FIELD_END_TIME);
    }
    
    @Test  
    public void addWithSameDates() throws ParseException {
        String startDate = "2015-05-06 09:55";

        StoryDTO formObject = StoryTestUtil.createFormObject(StoryTestUtil.ID, StoryTestUtil.TITLE, StoryTestUtil.DESCRIPTION, startDate, startDate);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", "/story/add");
        BindingResult result = bindAndValidate(mockRequest, formObject);

        RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();

        String view = controller.add(formObject, result, attributes);

        verifyZeroInteractions(serviceMock, messageSourceMock);

        assertEquals(StoryController.VIEW_ADD, view);
        assertFieldErrorsEmpty(result, FIELD_START_TIME, FIELD_END_TIME);
    }

    private void assertFeedbackMessage(RedirectAttributes attributes, String messageCode) {
        assertFlashMessages(attributes, messageCode, StoryController.FLASH_MESSAGE_KEY_FEEDBACK);
    }

    private void assertFieldErrors(BindingResult result, String... fieldNames) {
        assertEquals(fieldNames.length, result.getFieldErrorCount());
        for (String fieldName : fieldNames) {
            assertNotNull(result.getFieldError(fieldName));
        }
    }
    
    private void assertFieldErrorsEmpty(BindingResult result, String... fieldNames) {
        assertNotSame(fieldNames.length, result.getFieldErrorCount());
        for (String fieldName : fieldNames) {
            assertNull(result.getFieldError(fieldName));
        }
    }

    private void assertFlashMessages(RedirectAttributes attributes, String messageCode, String flashMessageParameterName) {
        Map<String, ?> flashMessages = attributes.getFlashAttributes();
        Object message = flashMessages.get(flashMessageParameterName);

        assertNotNull(message);
        flashMessages.remove(message);
        assertTrue(flashMessages.isEmpty());

        verify(messageSourceMock, times(1)).getMessage(eq(messageCode), any(Object[].class), any(Locale.class));
        verifyNoMoreInteractions(messageSourceMock);
    }

    private BindingResult bindAndValidate(HttpServletRequest request, Object formObject) {
        WebDataBinder binder = new WebDataBinder(formObject);
        binder.setValidator(validator);
        binder.bind(new MutablePropertyValues(request.getParameterMap()));
        binder.getValidator().validate(binder.getTarget(), binder.getBindingResult());
        return binder.getBindingResult();
    }

    private void initMessageSourceForFeedbackMessage(String feedbackMessageCode) {
        when(messageSourceMock.getMessage(eq(feedbackMessageCode), any(Object[].class), any(Locale.class))).thenReturn(FEEDBACK_MESSAGE);
    }
}
