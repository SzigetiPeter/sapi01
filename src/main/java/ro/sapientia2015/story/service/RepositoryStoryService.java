package ro.sapientia2015.story.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ro.sapientia2015.story.dto.StoryDTO;
import ro.sapientia2015.story.exception.NotFoundException;
import ro.sapientia2015.story.model.Story;
import ro.sapientia2015.story.repository.StoryRepository;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Kiss Tibor
 */
@Service
public class RepositoryStoryService implements StoryService {

	@Resource
	private StoryRepository repository;

	@Transactional
	@Override
	public Story add(StoryDTO added) {

		Story model = Story.getBuilder(added.getTitle())
				.description(added.getDescription())
				.startTime(added.getStartTime()).endTime(added.getEndTime())
				.build();

		return repository.save(model);
	}

	@Transactional(rollbackFor = { NotFoundException.class })
	@Override
	public Story deleteById(Long id) throws NotFoundException {
		Story deleted = findById(id);
		repository.delete(deleted);
		return deleted;
	}

	@Transactional(readOnly = true)
	@Override
	public List<Story> findAll() {
		return repository.findAll();
	}

	@Transactional(readOnly = true, rollbackFor = { NotFoundException.class })
	@Override
	public Story findById(Long id) throws NotFoundException {
		Story found = repository.findOne(id);
		if (found == null) {
			throw new NotFoundException("No entry found with id: " + id);
		}

		return found;
	}

	@Transactional(rollbackFor = { NotFoundException.class })
	@Override
	public Story update(StoryDTO updated) throws NotFoundException {
		Story model = findById(updated.getId());
		model.update(updated.getDescription(), updated.getTitle(),
				updated.getStartTime(), updated.getEndTime());

		return model;
	}

	@Transactional(readOnly = true, rollbackFor = { NotFoundException.class })
	@Override
	public List<Story> findByStartDate(final String date)
			throws NotFoundException {

		final List<Story> found = new ArrayList<Story>();
		for(Story t : repository.findAll()) {
			if (t.getStartTime().equals(date)) {
				found.add(t);
			}
		}

		if (found.isEmpty()) {
			throw new NotFoundException("No entry found with date: " + date);
		}

		return found;
	}

	@Transactional(readOnly = true, rollbackFor = { NotFoundException.class })
	@Override
	public List<Story> findByEndDate(final String date)
			throws NotFoundException {

		final List<Story> found = new ArrayList<Story>();
		for(Story t : repository.findAll()) {
			if (t.getEndTime().equals(date)) {
				found.add(t);
			}
		}

		if (found.isEmpty()) {
			throw new NotFoundException("No entry found with date: " + date);
		}

		return found;
	}
	
	@Transactional(rollbackFor = { NotFoundException.class })
	@Override
	public Story deleteByEndDate(String date) throws NotFoundException {
		Story deleted = findByStartDate(date).get(0);
		repository.delete(deleted);
		return deleted;
		}
	
	@Transactional(rollbackFor = { NotFoundException.class })
	@Override
	public Story deleteByStartDate(String date) throws NotFoundException {
		Story deleted = findByStartDate(date).get(0);
		repository.delete(deleted);
		return deleted;
		}

}
