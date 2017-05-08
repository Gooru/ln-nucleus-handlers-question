package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.converters.ConverterRegistry;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.converters.FieldConverter;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.validators.FieldSelector;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.validators.FieldValidator;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.validators.ValidatorRegistry;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ashish on 11/1/16.
 */
@Table("content")
public class AJEntityQuestion extends Model {

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
    private static final Logger LOGGER = LoggerFactory.getLogger(AJEntityQuestion.class);

    // FIELDS
    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String PUBLISH_DATE = "publish_date";
    private static final String PUBLISH_STATUS = "publish_status";
    private static final String DESCRIPTION = "description";
    private static final String ANSWER = "answer";
    private static final String METADATA = "metadata";
    private static final String TAXONOMY = "taxonomy";
    private static final String HINT_EXPLANATION_DETAIL = "hint_explanation_detail";
    private static final String THUMBNAIL = "thumbnail";
    private static final String LICENSE = "license";
    public static final String CREATOR_ID = "creator_id";
    public static final String CONTENT_SUBFORMAT = "content_subformat";
    private static final String VISIBLE_ON_PROFILE = "visible_on_profile";
    private static final String NARRATION = "narration";
    public static final String COLLECTION_ID = "collection_id";
    private static final String MODIFIER_ID = "modifier_id";
    public static final String IS_DELETED = "is_deleted";
    public static final String QUESTION = "question";
    public static final String COURSE_ID = "course_id";
    private static final String CONTENT_FORMAT = "content_format";
    private static final String TENANT = "tenant";
    private static final String TENANT_ROOT = "tenant_root";
    public static final String RUBRIC = "rubric";

    private static final String PUBLISH_STATUS_PUBLISHED = "published";
    public static final String OPEN_ENDED_QUESTION_SUBFORMAT = "open_ended_question";

    // QUERIES & FILTERS
    public static final String FETCH_ASSESSMENT_GRADING =
        "SELECT question.id FROM content question, collection collection WHERE question.collection_id = collection.id "
            + " AND collection.format = 'assessment' AND question.content_subformat = 'open_ended_question' AND "
            + "question.content_format = 'question' " + " AND collection.grading = 'teacher' AND "
            + "question.is_deleted = 'false' AND collection.is_deleted = 'false' AND question.collection_id IS NOT "
            + "NULL AND question.collection_id = ?::uuid";

    public static final String IS_VALID_ASSESSMENT =
        "select count(id) from collection where id = ?::uuid and format = 'assessment'::content_container_type and "
            + "is_deleted = false";
    public static final String UPDATE_ASSESSMENT_GRADING =
        "UPDATE collection SET grading = 'system' WHERE id = ?::uuid AND is_deleted = 'false'";
    public static final String UPDATE_CONTAINER_TIMESTAMP =
        "update collection set updated_at = now() where id = ?::uuid and is_deleted = 'false'";
    public static final String OPEN_ENDED_QUESTION_FILTER =
        "collection_id = ?::uuid and content_subformat = 'open_ended_question'::content_subformat_type and "
            + "is_deleted = false";

    public static final String VALIDATE_EXISTS_NON_DELETED =
        "select id, creator_id, publish_date, collection_id, course_id, title, content_subformat, unit_id, lesson_id from "
            + "content where content_format = " + "?::content_format_type and id = ?::uuid and is_deleted = ?";
    public static final String FETCH_QUESTION =
        "select id, title, publish_date, publish_status, description, answer, metadata, taxonomy, "
            + "hint_explanation_detail, thumbnail, narration, license, creator_id, content_subformat, "
            + "visible_on_profile, course_id, unit_id, lesson_id, collection_id, original_creator_id, "
            + "original_content_id, tenant, tenant_root from content where content_format = ?::content_format_type "
            + "and id = ?::uuid and is_deleted = ?";
    public static final String AUTH_FILTER = "id = ?::uuid and (owner_id = ?::uuid or collaborator ?? ?);";
    public static final String PUBLISHED_FILTER = "id = ?::uuid and publish_status = 'published'::publish_status_type;";

    // TABLES
    public static final String TABLE_COURSE = "course";
    public static final String TABLE_COLLECTION = "collection";

    private static final String ORIGINAL_CREATOR_ID = "original_creator_id";
    private static final String ORIGINAL_CONTENT_ID = "original_content_id";
    private static final String UPDATED_AT = "updated_at";
    private static final String URL = "url";
    private static final String CREATED_AT = "created_at";
    private static final String UNIT_ID = "unit_id";
    private static final String LESSON_ID = "lesson_id";
    private static final String SEQUENCE_ID = "sequence_id";
    private static final String IS_COPYRIGHT_OWNER = "is_copyright_owner";
    private static final String COPYRIGHT_OWNER = "copyright_owner";
    private static final String INFO = "info";
    private static final String DISPLAY_GUIDE = "display_guide";
    private static final String ACCESSIBILITY = "accessibility";

    // FIELD LISTS
    public static final List<String> FETCH_QUESTION_FIELDS = Arrays
        .asList(ID, TITLE, PUBLISH_DATE, DESCRIPTION, ANSWER, METADATA, TAXONOMY, HINT_EXPLANATION_DETAIL, THUMBNAIL,
            NARRATION, LICENSE, CREATOR_ID, CONTENT_SUBFORMAT, VISIBLE_ON_PROFILE, COURSE_ID, UNIT_ID, LESSON_ID,
            COLLECTION_ID, ORIGINAL_CONTENT_ID, ORIGINAL_CREATOR_ID);

    // What fields are allowed in request payload. Note this does not include the auto populate fields
    private static final List<String> INSERT_QUESTION_ALLOWED_FIELDS = Arrays
        .asList(TITLE, DESCRIPTION, CONTENT_SUBFORMAT, ANSWER, METADATA, TAXONOMY, NARRATION, HINT_EXPLANATION_DETAIL,
            THUMBNAIL, VISIBLE_ON_PROFILE);
    private static final List<String> UPDATE_QUESTION_ALLOWED_FIELDS = Arrays
        .asList(TITLE, DESCRIPTION, ANSWER, METADATA, TAXONOMY, HINT_EXPLANATION_DETAIL, THUMBNAIL, VISIBLE_ON_PROFILE,
            NARRATION);

    public static final List<String> INSERT_QUESTION_FORBIDDEN_FIELDS = Arrays
        .asList(ID, URL, CREATED_AT, UPDATED_AT, CREATOR_ID, MODIFIER_ID, ORIGINAL_CREATOR_ID, ORIGINAL_CONTENT_ID,
            PUBLISH_DATE, CONTENT_FORMAT, COURSE_ID, UNIT_ID, LESSON_ID, COLLECTION_ID, SEQUENCE_ID, IS_COPYRIGHT_OWNER,
            COPYRIGHT_OWNER, INFO, DISPLAY_GUIDE, ACCESSIBILITY, IS_DELETED);
    public static final List<String> INSERT_QUESTION_MANDATORY_FIELDS =
        Arrays.asList(TITLE, DESCRIPTION, CONTENT_SUBFORMAT);

    public static final List<String> QUESTION_TYPES = Arrays
        .asList("multiple_choice_question", "multiple_answer_question", "true_false_question",
            "fill_in_the_blank_question", "open_ended_question", "hot_text_reorder_question",
            "hot_text_highlight_question", "hot_spot_image_question", "hot_spot_text_question", "external_question");

    public static final List<String> RUBRIC_ASSOCIATION_ALLOWED_TYPES = Arrays.asList("open_ended_question"); 
    
    // TYPES
    private static final String UUID_TYPE = "uuid";
    private static final String JSONB_TYPE = "jsonb";
    private static final String CONTENT_FORMAT_TYPE = "content_format_type";
    private static final String CONTENT_SUBFORMAT_TYPE = "content_subformat_type";

    private static final Map<String, FieldValidator> validatorRegistry;
    private static final Map<String, FieldConverter> converterRegistry;

    static {
        validatorRegistry = initializeValidators();
        converterRegistry = initializeConverters();
    }

    private static Map<String, FieldConverter> initializeConverters() {
        Map<String, FieldConverter> converterMap = new HashMap<>();
        converterMap.put(ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(METADATA, (FieldConverter::convertFieldToJson));
        converterMap.put(TAXONOMY, (FieldConverter::convertFieldToJson));
        converterMap.put(CREATOR_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(MODIFIER_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(CONTENT_FORMAT,
            (fieldValue -> FieldConverter.convertFieldToNamedType(fieldValue, CONTENT_FORMAT_TYPE)));
        converterMap.put(CONTENT_SUBFORMAT,
            (fieldValue -> FieldConverter.convertFieldToNamedType(fieldValue, CONTENT_SUBFORMAT_TYPE)));
        converterMap.put(ANSWER, (FieldConverter::convertFieldToJson));
        converterMap.put(HINT_EXPLANATION_DETAIL, (FieldConverter::convertFieldToJson));
        converterMap.put(TENANT, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(TENANT_ROOT, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        return Collections.unmodifiableMap(converterMap);
    }

    private static Map<String, FieldValidator> initializeValidators() {
        Map<String, FieldValidator> validatorMap = new HashMap<>();
        validatorMap.put(ID, (FieldValidator::validateUuid));
        validatorMap.put(TITLE, (value) -> FieldValidator.validateString(value, 1000));
        validatorMap.put(DESCRIPTION, (value) -> FieldValidator.validateString(value, 20000));
        validatorMap.put(CONTENT_SUBFORMAT, (value) -> QUESTION_TYPES.contains(value));
        validatorMap.put(ANSWER, FieldValidator::validateJsonArrayIfPresent);
        validatorMap.put(METADATA, FieldValidator::validateJsonIfPresent);
        validatorMap.put(TAXONOMY, FieldValidator::validateJsonIfPresent);
        validatorMap.put(NARRATION, (value) -> FieldValidator.validateStringIfPresent(value, 5000));
        validatorMap.put(HINT_EXPLANATION_DETAIL, FieldValidator::validateJsonIfPresent);
        validatorMap.put(THUMBNAIL, (value) -> FieldValidator.validateStringIfPresent(value, 2000));
        validatorMap.put(VISIBLE_ON_PROFILE, FieldValidator::validateBooleanIfPresent);
        validatorMap.put(TENANT, (FieldValidator::validateUuid));
        validatorMap.put(TENANT_ROOT, (FieldValidator::validateUuid));
        return Collections.unmodifiableMap(validatorMap);
    }

    public static FieldSelector editFieldSelector() {
        return () -> Collections.unmodifiableSet(new HashSet<>(UPDATE_QUESTION_ALLOWED_FIELDS));
    }

    public static FieldSelector createFieldSelector() {
        return new FieldSelector() {
            @Override
            public Set<String> allowedFields() {
                return Collections.unmodifiableSet(new HashSet<>(INSERT_QUESTION_ALLOWED_FIELDS));
            }

            @Override
            public Set<String> mandatoryFields() {
                return Collections.unmodifiableSet(new HashSet<String>(INSERT_QUESTION_MANDATORY_FIELDS));
            }
        };
    }

    public void setModifierId(String modifier) {
        setFieldUsingConverter(MODIFIER_ID, modifier);
    }

    public void setCreatorId(String creator) {
        setFieldUsingConverter(CREATOR_ID, creator);
    }

    public void setContentFormatQuestion() {
        setFieldUsingConverter(CONTENT_FORMAT, QUESTION);
    }

    public void setTenant(String tenant) {
        setFieldUsingConverter(TENANT, tenant);
    }

    public void setTenantRoot(String tenantRoot) {
        setFieldUsingConverter(TENANT_ROOT, tenantRoot);
    }

    public void setLicense(Integer code) {
        this.set(LICENSE, code);
    }

    public String getTenant() {
        return this.getString(TENANT);
    }

    public String getTenantRoot() {
        return this.getString(TENANT_ROOT);
    }
    
    public boolean isQuestionPublished() {
        String publishStatus = this.getString(PUBLISH_STATUS);
        return PUBLISH_STATUS_PUBLISHED.equalsIgnoreCase(publishStatus);
    }

    private void setFieldUsingConverter(String fieldName, Object fieldValue) {
        FieldConverter fc = converterRegistry.get(fieldName);
        if (fc != null) {
            this.set(fieldName, fc.convertField(fieldValue));
        } else {
            this.set(fieldName, fieldValue);
        }
    }

    public static ValidatorRegistry getValidatorRegistry() {
        return new QuestionValidationRegistry();
    }

    public static ConverterRegistry getConverterRegistry() {
        return new QuestionConverterRegistry();
    }

    public String getCourseId() {
        return this.getString(COURSE_ID);
    }
    
    public String getUnitId() {
        return this.getString(UNIT_ID);
    }
    
    public String getLessonId() {
        return this.getString(LESSON_ID);
    }

    public String getCollectionId() {
        return this.getString(COLLECTION_ID);
    }

    private static class QuestionValidationRegistry implements ValidatorRegistry {
        @Override
        public FieldValidator lookupValidator(String fieldName) {
            return validatorRegistry.get(fieldName);
        }
    }

    private static class QuestionConverterRegistry implements ConverterRegistry {
        @Override
        public FieldConverter lookupConverter(String fieldName) {
            return converterRegistry.get(fieldName);
        }
    }

}
