package com.tfg.example.mapper;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Questionnaire;
import org.hl7.fhir.r5.model.QuestionnaireResponse;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r5.model.Questionnaire.QuestionnaireItemComponent;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class QuestResponseToQuest implements IMapper<QuestionnaireResponse, Questionnaire> {

	private FhirContext ctx = FhirContext.forR5();
	private IGenericClient client = ctx.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
	
	@Override
	public Questionnaire map(QuestionnaireResponse in) {
		String id = getQuest(in);
		Questionnaire questionnaire = client.read().resource(Questionnaire.class).withId(id).execute();
		
		return questionnaire;
	}
	
	private String getQuest(QuestionnaireResponse response) {
		String id = null;
		Questionnaire questionnaire = new Questionnaire();
		Questionnaire quest_old = client.read().resource(Questionnaire.class).withUrl(response.getQuestionnaire()).execute();
		
		questionnaire.setStatus(PublicationStatus.ACTIVE);
		
		for (QuestionnaireResponse.QuestionnaireResponseItemComponent item : response.getItem()) {
			Questionnaire.QuestionnaireItemComponent it = null;
			
			for (Questionnaire.QuestionnaireItemComponent item_old : quest_old.getItem()) {
				if (item_old.getLinkId().equals(item.getLinkId())) {
					it = item_old;
				}
			}
				
			switch (it.getType()) {
			case BOOLEAN:
				questionnaire.addItem()
					.setLinkId(item.getLinkId())
					.setText(item.getText())
					.setType(it.getType())
					.addAnswerOption().setValue(new StringType(Boolean.toString(item.getAnswer().get(0).getValueBooleanType().booleanValue())));
				break;
			case INTEGER:
				questionnaire.addItem()
					.setLinkId(item.getLinkId())
					.setText(item.getText())
					.setType(it.getType())
					.addAnswerOption().setValue(item.getAnswer().get(0).getValue());
				break;
			case STRING:
				questionnaire.addItem()
					.setLinkId(item.getLinkId())
					.setText(item.getText())
					.setType(it.getType())
					.addAnswerOption().setValue(item.getAnswer().get(0).getValue());
				break;
			case DATE:
				questionnaire.addItem()
					.setLinkId(item.getLinkId())
					.setText(item.getText())
					.setType(it.getType())
					.addAnswerOption().setValue(item.getAnswer().get(0).getValue());
				break;
			case CHOICE:
				addChoice(questionnaire, item, it, null);
				break;
			case GROUP:
				addGroup(questionnaire, item, it);
				break;
			default:
				throw new UnsupportedOperationException("Tipo de componente no soportado: " + it.getType().getDisplay());
			}
		}
		
		id = createQuest(questionnaire);
		
		return id;
	}
	
	private String createQuest(Questionnaire questionnaire) {
		String id = null;
		
		MethodOutcome outcome = client.create().resource(questionnaire).execute();
     	id = outcome.getId().getIdPart();
     	
     	return id;
	}
		
	private void addGroup(Questionnaire cuestionario, QuestionnaireResponse.QuestionnaireResponseItemComponent item, Questionnaire.QuestionnaireItemComponent it) {
		List<QuestionnaireItemComponent> example = new ArrayList<Questionnaire.QuestionnaireItemComponent>();
		
		for (QuestionnaireResponse.QuestionnaireResponseItemComponent item1 : item.getItem()) {
			Questionnaire.QuestionnaireItemComponent it1 = null;
			
			for (Questionnaire.QuestionnaireItemComponent item_old : it.getItem()) {
				if (item_old.getLinkId().equals(item1.getLinkId())) {
					it1 = item_old;
				}
			}
			
			QuestionnaireItemComponent t = new QuestionnaireItemComponent();
			switch (it1.getType()) {
			case BOOLEAN:
				t.setLinkId(item1.getLinkId())
					.setText(item1.getText())
					.setType(it1.getType())
					.addAnswerOption().setValue(new StringType(Boolean.toString(item1.getAnswer().get(0).getValueBooleanType().booleanValue())));
				example.add(t);
				break;
			case INTEGER:
				t.setLinkId(item1.getLinkId())
					.setText(item1.getText())
					.setType(it1.getType())
					.addAnswerOption().setValue(item1.getAnswer().get(0).getValue());
				example.add(t);
				break;
			case STRING:
				t.setLinkId(item1.getLinkId())
					.setText(item1.getText())
					.setType(it1.getType())
					.addAnswerOption().setValue(item1.getAnswer().get(0).getValue());
				example.add(t);
				break;
			case DATE:
				t.setLinkId(item1.getLinkId())
					.setText(item1.getText())
					.setType(it1.getType())
					.addAnswerOption().setValue(item1.getAnswer().get(0).getValue());
				example.add(t);
				break;
			case CHOICE:
				addChoice(cuestionario, item1, it1, t);
				example.add(t);
				break;
			case GROUP:
				addGroup(cuestionario, item1, it1);
				break;
			default:
				throw new UnsupportedOperationException("Tipo de componente no soportado: " + it.getType().getDisplay());
			}
		}
		
		cuestionario.addItem()
			.setLinkId(item.getLinkId())
			.setText(item.getText())
			.setType(it.getType())
			.setItem(example);
	}
		
	private void addChoice(Questionnaire cuestionario, QuestionnaireResponse.QuestionnaireResponseItemComponent item, Questionnaire.QuestionnaireItemComponent it, QuestionnaireItemComponent t) {
		if (t == null) {
			cuestionario.addItem()
				.setLinkId(item.getLinkId())
				.setText(item.getText())
				.setType(it.getType())
				.setAnswerOption(it.getAnswerOption())
				.addAnswerOption().setValue(new Coding().setCode(item.getAnswer().get(0).getValueCoding().getCode()));
		} else {
			t.setLinkId(item.getLinkId())
				.setText(item.getText())
				.setType(it.getType())
				.setAnswerOption(it.getAnswerOption())
				.addAnswerOption().setValue(new Coding().setCode(item.getAnswer().get(0).getValueCoding().getCode()));
		}
	}
}