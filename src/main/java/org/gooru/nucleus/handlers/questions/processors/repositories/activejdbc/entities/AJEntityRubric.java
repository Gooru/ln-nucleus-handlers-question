package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.converters.ConverterRegistry;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.converters.FieldConverter;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.validators.FieldSelector;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.validators.FieldValidator;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.validators.ValidatorRegistry;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * @author szgooru Created On: 28-Feb-2017
 */
@Table("rubric")
public class AJEntityRubric extends Model {

    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String URL = "url";
    public static final String IS_REMOTE = "is_remote";
    public static final String DESCRIPTION = "description";
    public static final String CATEGORIES = "categories";
    public static final String FEEDBACK_GUIDANCE = "feedback_guidance";
    public static final String OVERALL_FEEDBACK_REQUIRED = "overall_feedback_required";
    public static final String CREATOR_ID = "creator_id";
    public static final String MODIFIER_ID = "modifier_id";
    public static final String ORIGINAL_CREATOR_ID = "original_creator_id";
    public static final String ORIGINAL_RUBRIC_ID = "original_rubric_id";
    public static final String PARENT_RUBRIC_ID = "parent_rubric_id";
    public static final String PUBLISH_DATE = "publish_date";
    public static final String PUBLISH_STATUS = "publish_status";
    public static final String METADATA = "metadata";
    public static final String TAXONOMY = "taxonomy";
    public static final String GUT_CODES = "gut_codes";
    public static final String THUMBNAIL = "thumbnail";
    public static final String COURSE_ID = "course_id";
    public static final String UNIT_ID = "unit_id";
    public static final String LESSON_ID = "lesson_id";
    public static final String COLLECTION_ID = "collection_id";
    public static final String CONTENT_ID = "content_id";
    public static final String IS_RUBRIC = "is_rubric";
    public static final String SCORING = "scoring";
    public static final String MAX_SCORE = "max_score";
    public static final String INCREMENT = "increment";
    public static final String GRADER = "grader";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
    public static final String TENANT = "tenant";
    public static final String TENANT_ROOT = "tenant_root";
    public static final String VISIBLE_ON_PROFILE = "visible_on_profile";
    public static final String IS_DELETED = "is_deleted";
    public static final String CREATOR_SYSTEM = "creator_system";

    public static final String DUPLICATE_IDS = "duplicate_ids";

    public static final List<String> VALID_GRADER = Arrays.asList("Self", "Teacher");

    public static final String FETCH_RUBRIC =
        "SELECT id, title, url, is_remote, description, categories, feedback_guidance, overall_feedback_required,"
            + " is_rubric, course_id, unit_id, lesson_id, collection_id, content_id, scoring, max_score, increment, array_to_json(gut_codes) as gut_codes,"
            + " creator_id, modifier_id, original_creator_id, original_rubric_id, parent_rubric_id, publish_date, publish_status, metadata, taxonomy,"
            + " thumbnail, created_at, updated_at, tenant, tenant_root, visible_on_profile, is_deleted, creator_system FROM rubric"
            + " WHERE id = ?::uuid AND is_deleted = false";

    public static final String FETCH_RUBRIC_SUMMARY =
        "SELECT id, title, url, is_remote, description, categories, feedback_guidance, overall_feedback_required, creator_id, modifier_id,"
            + " original_creator_id, original_rubric_id, parent_rubric_id, publish_status, metadata, taxonomy, thumbnail,"
            + " created_at, updated_at, tenant, visible_on_profile, increment, course_id, unit_id, lesson_id, array_to_json(gut_codes) as gut_codes,"
            + " collection_id, content_id, is_rubric, scoring, max_score, grader FROM rubric WHERE content_id = ?::uuid AND is_deleted = false";

    public static final List<String> RUBRIC_SUMMARY =
        Arrays.asList(ID, TITLE, URL, IS_REMOTE, DESCRIPTION, CATEGORIES, FEEDBACK_GUIDANCE, OVERALL_FEEDBACK_REQUIRED,
            CREATOR_ID, MODIFIER_ID, ORIGINAL_CREATOR_ID, ORIGINAL_RUBRIC_ID, PARENT_RUBRIC_ID, PUBLISH_STATUS, GUT_CODES,
            METADATA, TAXONOMY, THUMBNAIL, CREATED_AT, UPDATED_AT, TENANT, VISIBLE_ON_PROFILE, INCREMENT,
            COURSE_ID, UNIT_ID, LESSON_ID, COLLECTION_ID, CONTENT_ID, IS_RUBRIC, SCORING, MAX_SCORE, GRADER);

    public static final String SELECT_DUPLICATE =
        "SELECT id FROM rubric WHERE lower(url) = ? AND tenant = ?::uuid AND is_deleted = false AND original_rubric_id IS NULL";

    public static final String VALIDATE_EXISTS_NOT_DELETED =
        "SELECT id, url, is_remote, creator_id, original_rubric_id, content_id, is_rubric FROM rubric WHERE id = ?::uuid AND is_deleted = false";

    public static final String SELECT_EXISTING_RUBRIC_FOR_QUESTION =
        "SELECT id FROM rubric WHERE content_id = ?::uuid AND is_deleted = false";

    public static final String UPDATE_RUBRIC_MARK_DELETED = "is_deleted = true, modifier_id = ?::uuid";

    public static final String UPDATE_RUBRIC_MARK_DELETED_CONDITION = "id = ?::uuid";

    public static final String COPY_RUBRIC =
        "INSERT INTO rubric(id, title, url, is_remote, description, categories, feedback_guidance, overall_feedback_required,"
            + " creator_id, modifier_id, original_creator_id, original_rubric_id, parent_rubric_id, metadata, taxonomy,"
            + " gut_codes, thumbnail, created_at, updated_at, tenant, tenant_root, visible_on_profile, is_deleted, creator_system) SELECT ?, title,"
            + " url, is_remote, description, categories, feedback_guidance, overall_feedback_required, ?::uuid, ?::uuid,"
            + " coalesce(original_creator_id,creator_id) as original_creator_id, coalesce(original_rubric_id,?::uuid) as original_rubric_id, ?::uuid,"
            + " metadata, taxonomy, gut_codes, thumbnail, created_at, updated_at, ?::uuid, ?::uuid, visible_on_profile, is_deleted,"
            + " creator_system FROM rubric WHERE id = ?::uuid AND is_deleted = false";

    // Rubric ON Fields

    private static final List<String> INSERT_RUBRIC_ON_MANDATORY_FIELDS = Arrays.asList(TITLE, IS_RUBRIC);

    private static final List<String> INSERT_RUBRIC_ON_ALLOWED_FIELDS = Arrays.asList(TITLE, DESCRIPTION, METADATA,
        TAXONOMY, THUMBNAIL, IS_RUBRIC, OVERALL_FEEDBACK_REQUIRED, FEEDBACK_GUIDANCE, GRADER);

    private static final List<String> UPDATE_RUBRIC_ALLOWED_FIELDS =
        Arrays.asList(TITLE, DESCRIPTION, METADATA, TAXONOMY, THUMBNAIL, URL, IS_REMOTE, FEEDBACK_GUIDANCE, SCORING,
            MAX_SCORE, INCREMENT, GRADER, OVERALL_FEEDBACK_REQUIRED, CATEGORIES, VISIBLE_ON_PROFILE);

    public static final List<String> FETCH_RUBRIC_ON_FIELDS =
        Arrays.asList(ID, TITLE, URL, IS_REMOTE, DESCRIPTION, IS_RUBRIC, GRADER, CATEGORIES, FEEDBACK_GUIDANCE,
            OVERALL_FEEDBACK_REQUIRED, CREATOR_ID, MODIFIER_ID, ORIGINAL_CREATOR_ID, ORIGINAL_RUBRIC_ID, GUT_CODES,
            PARENT_RUBRIC_ID, PUBLISH_DATE, PUBLISH_STATUS, METADATA, TAXONOMY, THUMBNAIL, CREATED_AT,
            UPDATED_AT, TENANT, TENANT_ROOT, VISIBLE_ON_PROFILE, IS_DELETED, CREATOR_SYSTEM);

    // Rubric OFF Fields

    private static final List<String> INSERT_RUBRIC_OFF_MANDATORY_FIELDS = Arrays.asList(IS_RUBRIC);

    private static final List<String> INSERT_RUBRIC_OFF_ALLOWED_FIELDS =
        Arrays.asList(IS_RUBRIC, OVERALL_FEEDBACK_REQUIRED, FEEDBACK_GUIDANCE, SCORING, MAX_SCORE, INCREMENT, GRADER);
    
    private static final List<String> ASSOCIATE_ALLOWED_FIELDS =
        Arrays.asList(COURSE_ID, UNIT_ID, LESSON_ID, COLLECTION_ID);

    public static final List<String> FETCH_RUBRIC_OFF_FIELDS = Arrays.asList(ID, IS_RUBRIC, COURSE_ID, UNIT_ID,
        LESSON_ID, COLLECTION_ID, CONTENT_ID, SCORING, MAX_SCORE, INCREMENT, GRADER, FEEDBACK_GUIDANCE,
        OVERALL_FEEDBACK_REQUIRED, CREATOR_ID, MODIFIER_ID, CREATED_AT, UPDATED_AT, TENANT, TENANT_ROOT);

    public static final List<String> INSERT_RUBRIC_FORBIDDEN_FIELDS =
        Arrays.asList(ID, URL, IS_REMOTE, CATEGORIES, CREATED_AT, UPDATED_AT, CREATOR_ID, MODIFIER_ID,
            ORIGINAL_CREATOR_ID, ORIGINAL_RUBRIC_ID, PARENT_RUBRIC_ID, COURSE_ID, UNIT_ID, LESSON_ID, COLLECTION_ID,
            CONTENT_ID, GUT_CODES, PUBLISH_DATE, IS_DELETED, VISIBLE_ON_PROFILE, TENANT, TENANT_ROOT);

    // Scoring fields
    private static final List<String> SCORING_MANDATORY_FIELDS = Arrays.asList(SCORING);
    private static final List<String> SCORING_ALLOWED_FIELDS = Arrays.asList(SCORING, MAX_SCORE, INCREMENT);
    public static final List<String> SCORING_FIELDS = Arrays.asList(ID, SCORING, MAX_SCORE, INCREMENT, IS_RUBRIC);
    
    private static final Map<String, FieldValidator> validatorRegistry;
    private static final Map<String, FieldConverter> converterRegistry;

    static {
        validatorRegistry = initializeValidators();
        converterRegistry = initializeConverters();
    }

    private static Map<String, FieldConverter> initializeConverters() {
        Map<String, FieldConverter> converterMap = new HashMap<>();
        converterMap.put(ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(CATEGORIES, (FieldConverter::convertFieldToJson));
        converterMap.put(METADATA, (FieldConverter::convertFieldToJson));
        converterMap.put(TAXONOMY, (FieldConverter::convertFieldToJson));
        converterMap.put(DESCRIPTION, (fieldValue -> FieldConverter.convertEmptyStringToNull((String) fieldValue)));
        converterMap.put(THUMBNAIL, (fieldValue -> FieldConverter.convertEmptyStringToNull((String) fieldValue)));
        converterMap.put(CREATOR_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(MODIFIER_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(TENANT, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(TENANT_ROOT, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(GUT_CODES, (fieldValue -> FieldConverter.convertFieldToTextArray((String) fieldValue)));
        converterMap.put(TAXONOMY, (FieldConverter::convertFieldToJson));
        converterMap.put(CATEGORIES, (FieldConverter::convertFieldToJson));
        converterMap.put(FEEDBACK_GUIDANCE,
            (fieldValue -> FieldConverter.convertEmptyStringToNull((String) fieldValue)));
        converterMap.put(COURSE_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(UNIT_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(LESSON_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(COLLECTION_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(CONTENT_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        return Collections.unmodifiableMap(converterMap);
    }

    private static Map<String, FieldValidator> initializeValidators() {
        Map<String, FieldValidator> validatorMap = new HashMap<>();
        validatorMap.put(ID, (FieldValidator::validateUuid));
        validatorMap.put(TITLE, (value) -> FieldValidator.validateString(value, 1000));
        validatorMap.put(DESCRIPTION, (value) -> FieldValidator.validateStringAllowNullOrEmpty(value, 20000));
        validatorMap.put(METADATA, FieldValidator::validateJsonIfPresent);
        validatorMap.put(TAXONOMY, FieldValidator::validateJsonIfPresent);
        validatorMap.put(THUMBNAIL, (value) -> FieldValidator.validateStringAllowNullOrEmpty(value, 2000));
        validatorMap.put(VISIBLE_ON_PROFILE, FieldValidator::validateBooleanIfPresent);
        validatorMap.put(TENANT, (FieldValidator::validateUuid));
        validatorMap.put(TENANT_ROOT, (FieldValidator::validateUuid));
        validatorMap.put(URL, (value) -> FieldValidator.validateStringIfPresent(value, 2000));
        validatorMap.put(IS_REMOTE, FieldValidator::validateBooleanIfPresent);
        validatorMap.put(FEEDBACK_GUIDANCE, (value) -> FieldValidator.validateStringAllowNullOrEmpty(value, 5000));
        validatorMap.put(OVERALL_FEEDBACK_REQUIRED, FieldValidator::validateBooleanIfPresent);
        validatorMap.put(CATEGORIES, FieldValidator::validateJsonArrayIfPresent);
        validatorMap.put(VISIBLE_ON_PROFILE, FieldValidator::validateBooleanIfPresent);
        validatorMap.put(COURSE_ID, (FieldValidator::validateUuidIfPresent));
        validatorMap.put(UNIT_ID, (FieldValidator::validateUuidIfPresent));
        validatorMap.put(LESSON_ID, (FieldValidator::validateUuidIfPresent));
        validatorMap.put(COLLECTION_ID, (FieldValidator::validateUuidIfPresent));
        validatorMap.put(CONTENT_ID, (FieldValidator::validateUuidIfPresent));
        validatorMap.put(IS_RUBRIC, FieldValidator::validateBoolean);
        validatorMap.put(SCORING, FieldValidator::validateBooleanIfPresent);
        validatorMap.put(MAX_SCORE, FieldValidator::validateIntegerIfPresent);
        validatorMap.put(INCREMENT, FieldValidator::validateDoubleIfPresent);
        validatorMap.put(GRADER, (value) -> (value != null && VALID_GRADER.contains((String) value)));
        return Collections.unmodifiableMap(validatorMap);
    }

    public static ValidatorRegistry getValidatorRegistry() {
        return new RubricValidationRegistry();
    }

    public static ConverterRegistry getConverterRegistry() {
        return new RubricConverterRegistry();
    }

    private static class RubricValidationRegistry implements ValidatorRegistry {
        @Override
        public FieldValidator lookupValidator(String fieldName) {
            return validatorRegistry.get(fieldName);
        }
    }

    private static class RubricConverterRegistry implements ConverterRegistry {
        @Override
        public FieldConverter lookupConverter(String fieldName) {
            return converterRegistry.get(fieldName);
        }
    }

    public static FieldSelector createRubricOnFieldSelector() {
        return new FieldSelector() {
            @Override
            public Set<String> allowedFields() {
                return Collections.unmodifiableSet(new HashSet<>(INSERT_RUBRIC_ON_ALLOWED_FIELDS));
            }

            @Override
            public Set<String> mandatoryFields() {
                return Collections.unmodifiableSet(new HashSet<String>(INSERT_RUBRIC_ON_MANDATORY_FIELDS));
            }
        };
    }

    public static FieldSelector createRubricOffFieldSelector() {
        return new FieldSelector() {

            @Override
            public Set<String> allowedFields() {
                return Collections.unmodifiableSet(new HashSet<>(INSERT_RUBRIC_OFF_ALLOWED_FIELDS));
            }

            @Override
            public Set<String> mandatoryFields() {
                return Collections.unmodifiableSet(new HashSet<String>(INSERT_RUBRIC_OFF_MANDATORY_FIELDS));
            }
        };
    }
    
    public static FieldSelector scoringFieldSelector() {
        return new FieldSelector() {

            @Override
            public Set<String> allowedFields() {
                return Collections.unmodifiableSet(new HashSet<>(SCORING_ALLOWED_FIELDS));
            }

            @Override
            public Set<String> mandatoryFields() {
                return Collections.unmodifiableSet(new HashSet<String>(SCORING_MANDATORY_FIELDS));
            }
        };
    }

    public static FieldSelector updateFieldSelector() {
        return () -> Collections.unmodifiableSet(new HashSet<>(UPDATE_RUBRIC_ALLOWED_FIELDS));
    }

    public static FieldSelector associateFieldSelector() {
        return () -> Collections.unmodifiableSet(new HashSet<>(ASSOCIATE_ALLOWED_FIELDS));
    }

    public void setModifierId(String modifier) {
        setFieldUsingConverter(MODIFIER_ID, modifier);
    }

    public void setCreatorId(String creator) {
        setFieldUsingConverter(CREATOR_ID, creator);
    }

    public void setTenant(String tenant) {
        setFieldUsingConverter(TENANT, tenant);
    }

    public void setTenantRoot(String tenantRoot) {
        setFieldUsingConverter(TENANT_ROOT, tenantRoot);
    }

    public void setGutCodes(String gutCodes) {
        setFieldUsingConverter(GUT_CODES, gutCodes);
    }

    public void setCourseId(String courseId) {
        setFieldUsingConverter(COURSE_ID, courseId);
    }

    public void setUnitId(String unitId) {
        setFieldUsingConverter(UNIT_ID, unitId);
    }

    public void setLessonId(String lessonId) {
        setFieldUsingConverter(LESSON_ID, lessonId);
    }

    public void setCollectionId(String collectionId) {
        setFieldUsingConverter(COLLECTION_ID, collectionId);
    }

    public void setContentId(String contentId) {
        setFieldUsingConverter(CONTENT_ID, contentId);
    }

    private void setFieldUsingConverter(String fieldName, Object fieldValue) {
        FieldConverter fc = converterRegistry.get(fieldName);
        if (fc != null) {
            this.set(fieldName, fc.convertField(fieldValue));
        } else {
            this.set(fieldName, fieldValue);
        }
    }
}
