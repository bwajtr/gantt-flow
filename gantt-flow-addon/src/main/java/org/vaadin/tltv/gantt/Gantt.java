package org.vaadin.tltv.gantt;

import java.text.DateFormatSymbols;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.vaadin.tltv.gantt.element.StepElement;
import org.vaadin.tltv.gantt.event.GanttClickEvent;
import org.vaadin.tltv.gantt.event.GanttDataChangeEvent;
import org.vaadin.tltv.gantt.event.StepClickEvent;
import org.vaadin.tltv.gantt.event.StepMoveEvent;
import org.vaadin.tltv.gantt.event.StepResizeEvent;
import org.vaadin.tltv.gantt.model.GanttStep;
import org.vaadin.tltv.gantt.model.Resolution;
import org.vaadin.tltv.gantt.model.Step;
import org.vaadin.tltv.gantt.model.SubStep;
import org.vaadin.tltv.gantt.util.GanttUtil;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonArray;
import elemental.json.impl.JreJsonFactory;

@Tag("gantt-element")
@NpmPackage(value = "tltv-gantt-element", version = "^1.0.13")
@NpmPackage(value = "tltv-timeline-element", version = "^1.0.13")
@JsModule("tltv-gantt-element/dist/src/gantt-element.js")
@CssImport(value = "gantt-grid.css", themeFor = "vaadin-grid")
public class Gantt extends Component implements HasSize {

	private final JreJsonFactory jsonFactory = new JreJsonFactory();

	public void setResolution(Resolution resolution) {
		getElement().setAttribute("resolution",
				Objects.requireNonNull(resolution, "Setting null Resolution is not allowed").name());
	}

	public Resolution getResolution() {
		return Resolution.valueOf(getElement().getAttribute("resolution"));
	}

	public void setLocale(Locale locale) {
		getElement().setAttribute("locale",
				Objects.requireNonNull(locale, "Setting null Locale is not allowed").toLanguageTag());
		setupByLocale();
	}

	public Locale getLocale() {
		return Locale.forLanguageTag(getElement().getAttribute("locale"));
	}

	public void setTimeZone(TimeZone timeZone) {
		getElement().setAttribute("zone",
				Objects.requireNonNull(timeZone, "Setting null TimeZone is not allowed").getID());
	}

	public TimeZone getTimeZone() {
		return TimeZone.getTimeZone(getElement().getAttribute("zone"));
	}

	public void setStartDate(LocalDate startDate) {
		getElement().setAttribute("start", GanttUtil.formatDateTime(resetTimeToMin(startDate.atStartOfDay())));
	}

	public void setStartDateTime(LocalDateTime startDateTime) {
		getElement().setAttribute("start", GanttUtil.formatDateTime(resetTimeToMin(startDateTime)));
	}

	public LocalDateTime getStartDateTime() {
		return LocalDateTime.from(GanttUtil.parseDateTime(getElement().getAttribute("start")));
	}

	public void setEndDate(LocalDate endDate) {
		getElement().setAttribute("end", GanttUtil.formatDateTime(resetTimeToMin(endDate.atStartOfDay())));
	}

	public void setEndDateTime(LocalDateTime endDateTime) {
		getElement().setAttribute("end", GanttUtil.formatDateTime(resetTimeToMin(endDateTime)));
	}

	public LocalDateTime getEndDateTime() {
		return LocalDateTime.from(GanttUtil.parseDateTime(getElement().getAttribute("end")));
	}

	public void setYearRowVisible(boolean visible) {
		getElement().setProperty("yearRowVisible", visible);
	}

	public boolean isYearRowVisible() {
		return getElement().getProperty("yearRowVisible", true);
	}

	public void setMonthRowVisible(boolean visible) {
		getElement().setProperty("monthRowVisible", visible);
	}

	public boolean isMonthRowVisible() {
		return getElement().getProperty("monthRowVisible", true);
	}

	public void setMovableSteps(boolean enabled) {
		getElement().setProperty("movableSteps", enabled);
	}
	
	public boolean isMovableSteps() {
		return getElement().getProperty("movableSteps", true); 
	}
	
	public void setResizableSteps(boolean enabled) {
		getElement().setProperty("resizableSteps", enabled);
	}
	
	public boolean isResizableSteps() {
		return getElement().getProperty("resizableSteps", true); 
	}
	
	public void setMovableStepsBetweenRows(boolean enabled) {
		getElement().setProperty("movableStepsBetweenRows", enabled);
	}
	
	public boolean isMovableStepsBetweenRows() {
		return getElement().getProperty("movableStepsBetweenRows", true); 
	}
	
	public void addSteps(Collection<Step> steps) {
		if(steps != null) {
			addSteps(steps.stream());
		}
	}
	
	public void addSteps(Step... steps) {
		if(steps != null) {
			addSteps(Stream.of(steps));
		}
	}
	
	public void addSteps(Stream<Step> steps) {
		if(steps == null) {
			return;
		}
		steps.forEach(this::appendStep);
		fireDataChangeEvent();
	}
	
	public void addStep(Step step) {
		appendStep(step);
		fireDataChangeEvent();
	}

	public void addSubStep(SubStep subStep) {
		StepElement ownerStepElement = getStepElements().collect(Collectors.toList())
				.get(indexOf(subStep.getOwner().getUid()));
		ownerStepElement.getElement().appendChild(new StepElement(ensureUID(subStep)).getElement());
	}
	
	public void addStep(int index, Step step) {
        if (contains(ensureUID(step))) {
            moveStep(index, step);
        } else {
        	getElement().insertChild(index, new StepElement(ensureUID(step)).getElement());
        	fireDataChangeEvent();
        }
    }

	public void moveStep(int toIndex, GanttStep anyStep) {
		if(anyStep.isSubstep()) {
			moveSubStep(toIndex, (SubStep) anyStep);
		} else {
			moveStep(toIndex, (Step) anyStep);
		}
	}
	
    public void moveStep(int toIndex, Step step) {
        if (!contains(step)) {
            return;
        }
        String targetStepUid = getStepElements().collect(Collectors.toList()).get(toIndex).getUid();
        int fromIndex = indexOf(step);
        Step moveStep = step;
        if (!targetStepUid.equals(moveStep.getUid())) {
        	var subStepEements = getSubStepElements(moveStep.getUid());
        	getStepElementOptional(moveStep.getUid()).ifPresent(StepElement::removeFromParent);
        	StepElement stepElement = new StepElement(moveStep);
        	subStepEements.forEach(subStepElement -> stepElement.getElement().appendChild(subStepElement.getElement()));
        	if(fromIndex <= toIndex) {
        		getElement().insertChild(indexOf(targetStepUid) + 1, stepElement.getElement());
        	} else {
        		getElement().insertChild(indexOf(targetStepUid), stepElement.getElement());
        	}
        	fireDataChangeEvent();
        }
        updateSubStepsByMovedOwner(moveStep.getUid());
    }
    
	public void moveSubStep(int toIndex, SubStep subStep) {
		if (!contains(subStep)) {
			return;
		}
		String targetStepUid = getStepElements().collect(Collectors.toList()).get(toIndex).getUid();
		StepElement stepElement = getStepElement(targetStepUid);
		Step moveStep = subStep.getOwner();
		if (!targetStepUid.equals(moveStep.getUid())) {
			getSubStepElements().filter(item -> item.getUid().equals(subStep.getUid())).findFirst()
					.ifPresent(StepElement::removeFromParent);
			subStep.setOwner(getStep(targetStepUid));
			stepElement.getElement().appendChild(new StepElement(subStep).getElement());
		}
		subStep.updateOwnerDatesBySubStep();
		stepElement.refresh();
    }

	public void removeSteps(Collection<Step> steps) {
		if(steps != null) {
			removeSteps(steps.stream());
		}
	}
	
	public void removeSteps(Step... steps) {
		if(steps != null) {
			removeSteps(Stream.of(steps));
		}
	}
	
	public void removeSteps(Stream<Step> steps) {
		if(steps == null) {
			return;
		}
		steps.forEach(this::doRemoveStep);
		fireDataChangeEvent();
	}
	
	public boolean removeStep(Step step) {
		if (step == null) {
			return false;
		}
		if(doRemoveStep(step)) {
			fireDataChangeEvent();
			return true;
		}
		return false;
	}

	private boolean doRemoveStep(Step step) {
		var removedStepElement = getStepElement(step.getUid());
		if (removedStepElement != null) {
			removedStepElement.removeFromParent();
			return true;
		}
		return false;
	}

	private void appendStep(Step step) {
		getElement().appendChild(new StepElement(ensureUID(step)).getElement());
	}

	private void setupByLocale() {
		setArrayProperty("monthNames", new DateFormatSymbols(getLocale()).getMonths());
		setArrayProperty("weekdayNames", new DateFormatSymbols(getLocale()).getWeekdays());
		// First day of week (1 = sunday, 2 = monday)
		final java.util.Calendar cal = new GregorianCalendar(getLocale());
		getElement().setProperty("firstDayOfWeek", cal.getFirstDayOfWeek() - 1);
	}

	private void setArrayProperty(String name, String[] array) {
		final JsonArray jsonArray = jsonFactory.createArray();
		for (int index = 0; index < array.length; index++) {
			jsonArray.set(index, array[index]);
		}
		getElement().executeJs("this." + name + " = $0;", jsonArray);
	}

	LocalDateTime resetTimeToMin(LocalDateTime dateTime) {
		return GanttUtil.resetTimeToMin(dateTime, getResolution());
	}

	LocalDateTime resetTimeToMax(LocalDateTime dateTime, boolean exclusive) {
		return GanttUtil.resetTimeToMax(dateTime, getResolution(), exclusive);
	}
	
	public StepElement getStepElement(String uid) {
		return getStepElementOptional(uid).orElse(null);
	}
	
	public Optional<StepElement> getStepElementOptional(String uid) {
		return getStepElements().filter(step -> Objects.equals(uid, step.getUid())).findFirst();
	}
	
    public Stream<StepElement> getStepElements() {
		return getChildren().filter(child -> child instanceof StepElement).map(StepElement.class::cast);
	}
    
    public Stream<Step> getSteps() {
		return getChildren().filter(child -> child instanceof StepElement).map(StepElement.class::cast)
				.map(StepElement::getModel).map(Step.class::cast);
	}
    
    public List<Step> getStepsList() {
    	return getSteps().collect(Collectors.toList());
    }
    
	public Stream<StepElement> getFlatStepElements() {
		Stream.Builder<StepElement> streamBuilder = Stream.builder();
		getStepElements().forEach(step -> {
			streamBuilder.add(step);
			step.getChildren().filter(child -> child instanceof StepElement).map(StepElement.class::cast)
					.forEach(streamBuilder::add);
		});
		return streamBuilder.build();
	}
    
	public Stream<StepElement> getSubStepElements(String forStepUid) {
		StepElement stepEl = getStepElement(forStepUid);
		if (stepEl != null) {
			return stepEl.getChildren().filter(child -> child instanceof StepElement).map(StepElement.class::cast);
		}
		return Stream.empty();
	}
	
    public Stream<StepElement> getSubStepElements() {
		return getStepElements().flatMap(step -> step.getChildren().filter(child -> child instanceof StepElement))
				.map(StepElement.class::cast);
	}

    public boolean contains(String targetUid) {
        return getFlatStepElements()
        		.anyMatch(step -> step.getUid().equals(targetUid));
    }
    
    public boolean contains(GanttStep targetStep) {
        return getFlatStepElements()
        		.anyMatch(step -> step.getUid().equals(targetStep.getUid()));
    }
    
	public boolean contains(Step targetStep) {
		return contains(getStepElements(), targetStep.getUid());
    }
	
	public boolean contains(SubStep targetSubStep) {
        return contains(getSubStepElements(), targetSubStep.getUid());
    }
	
	private boolean contains(Stream<StepElement> stream, String targetUid) {
        return stream
        		.filter(child -> child instanceof StepElement).map(StepElement.class::cast)
        		.anyMatch(step -> step.getUid().equals(targetUid));
    }
    
	public int indexOf(Step step) {
		return indexOf(step.getUid());
	}
	
    public int indexOf(String stepUid) {
    	GanttStep step = getAnyStep(stepUid);
    	if(step.isSubstep()) {
    		step = ((SubStep) step).getOwner();
    	}
    	List<String> uidList = getStepElements().map(StepElement::getUid).collect(Collectors.toList());
        return uidList.indexOf(step.getUid());
    }
    
    public SubStep getSubStep(String uid) {
		return getSubStepElements().filter(step -> Objects.equals(uid, step.getUid())).findFirst()
				.map(StepElement::getModel).map(SubStep.class::cast).orElse(null);
	}
    
	public Step getStep(String uid) {
		return getStepElements().filter(step -> Objects.equals(uid, step.getUid())).findFirst()
				.map(StepElement::getModel).map(Step.class::cast).orElse(null);
	}
    
    public GanttStep getAnyStep(String uid) {
    	return getFlatStepElements().filter(step -> Objects.equals(uid, step.getUid())).findFirst()
				.map(StepElement::getModel).orElse(null);
    }

    public void updateSubStepsByMovedOwner(String stepUid) {
    	Step step = getStep(stepUid);
		// update sub-steps by moved owner
		LocalDateTime previousStart = getSubStepElements(stepUid).map(StepElement::getModel)
				.map(GanttStep::getStartDate).min(Comparator.naturalOrder()).orElse(step.getStartDate());
		Duration delta = Duration.between(previousStart, step.getStartDate());
		getSubStepElements(stepUid).forEach(substep -> {
			substep.getModel().setStartDate(substep.getModel().getStartDate().plus(delta));
			substep.getModel().setEndDate(substep.getModel().getEndDate().plus(delta));
			substep.refresh();
		});
    }
    
	/**
	 * Refresh target step element if it exists.
	 * 
	 * @param uid Target step UID
	 */
	public void refresh(String uid) {
		var stepElement = getStepElement(uid);
		if (stepElement != null) {
			stepElement.refresh();
		}
	}
    
	/**
     * Ensures that given step has UID. If not, then generates one.
     */
    protected <T extends GanttStep> T ensureUID(T step) {
        if (step == null) {
            return null;
        }
        if (step.getUid() == null || step.getUid().isEmpty()) {
            step.setUid(UUID.randomUUID().toString());
        }
        return step;
    }
    
    @Override
    public void setWidth(String width) {
    	getElement().getStyle().set("--gantt-element-width", Objects.requireNonNullElse(width, "auto"));
    	getElement().callJsFunction("updateSize");
    }
    
    @Override
    public void setHeight(String height) {
    	getElement().getStyle().set("--gantt-element-height", Objects.requireNonNullElse(height, "auto"));
    	getElement().callJsFunction("updateSize");
    }
    
	public Registration addGanttClickListener(ComponentEventListener<GanttClickEvent> listener) {
		return addListener(GanttClickEvent.class, listener);
	}
	
	public Registration addStepClickListener(ComponentEventListener<StepClickEvent> listener) {
		return addListener(StepClickEvent.class, listener);
	}
	
	public Registration addStepMoveListener(ComponentEventListener<StepMoveEvent> listener) {
		return addListener(StepMoveEvent.class, listener);
	}
	
	public Registration addStepResizeListener(ComponentEventListener<StepResizeEvent> listener) {
		return addListener(StepResizeEvent.class, listener);
	}
	
	public Registration addDataChangeListener(ComponentEventListener<GanttDataChangeEvent> listener) {
		return addListener(GanttDataChangeEvent.class, listener);
	}
	
	public Grid<Step> buildCaptionGrid(String header) {
		var grid = new Grid<Step>();
		grid.addClassName("gantt-caption-grid");
		grid.addColumn(LitRenderer.<Step> of("<span>${item.caption}</span>").withProperty("caption", Step::getCaption)).setHeader(header);
		grid.setItems(query -> getSteps().limit(query.getLimit()).skip(query.getOffset()));
		addDataChangeListener(event -> grid.getLazyDataView().refreshAll());
		getElement().executeJs("this.registerScrollElement($0.$.table)", grid);
		return grid;
	}
	
	private void fireDataChangeEvent() {
		fireEvent(new GanttDataChangeEvent(this));
	}
}
