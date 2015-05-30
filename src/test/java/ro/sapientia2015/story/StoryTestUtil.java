package ro.sapientia2015.story;

import java.util.Date;

import org.springframework.test.util.ReflectionTestUtils;

import ro.sapientia2015.story.dto.StoryDTO;
import ro.sapientia2015.story.model.Story;

public class StoryTestUtil {

    public static final Long ID = 1L;
    public static final String DESCRIPTION = "description";
    public static final String DESCRIPTION_UPDATED = "updatedDescription";
    public static final String TITLE = "title";
    public static final String TITLE_UPDATED = "updatedTitle";
    public static final String START_DATE = "2015-05-15 09:55";
    public static final String START_DATE_UPDATED = "2015-05-16 09:55";
    public static final String END_DATE = "2015-05-18 09:55";
    public static final String END_DATE_UPDATED = "2015-05-19 10:55";

    private static final String CHARACTER = "a";

    public static StoryDTO createFormObject(Long id, String description, String title) {
        StoryDTO dto = new StoryDTO();

        dto.setId(id);
        dto.setDescription(description);
        dto.setTitle(title);

        return dto;
    }
    
    public static StoryDTO createFormObject(Long id, String description, String title, String startTime, String endTime) {
        StoryDTO dto = new StoryDTO();

        dto.setId(id);
        dto.setDescription(description);
        dto.setTitle(title);
        dto.setStartTime(startTime);
        dto.setEndTime(endTime);
        	
        return dto;
    }

    public static Story createModel(Long id, String description, String title) {
        Story model = Story.getBuilder(title)
                .description(description)
                .build();

        ReflectionTestUtils.setField(model, "id", id);

        return model;
    }
    
    public static Story createModel(Long id, String description, String title, String startTime, String endTime) {
        Story model = Story.getBuilder(title)
                .description(description)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        ReflectionTestUtils.setField(model, "id", id);

        return model;
    }

    public static String createRedirectViewPath(String path) {
        StringBuilder redirectViewPath = new StringBuilder();
        redirectViewPath.append("redirect:");
        redirectViewPath.append(path);
        return redirectViewPath.toString();
    }

    public static String createStringWithLength(int length) {
        StringBuilder builder = new StringBuilder();

        for (int index = 0; index < length; index++) {
            builder.append(CHARACTER);
        }

        return builder.toString();
    }

}
