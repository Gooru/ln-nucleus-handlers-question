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
    public static final String TYPE = "type";
    public static final String FEEDBACK_GUIDANCE = "feedback_guidance";
    public static final String TOTAL_POINTS = "total_points";
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
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
    public static final String TENANT = "tenant";
    public static final String TENANT_ROOT = "tenant_root";
    public static final String VISIBLE_ON_PROFILE = "visible_on_profile";
    public static final String IS_DELETED = "is_deleted";
    public static final String CREATOR_SYSTEM = "creator_system";

    private static final String RUBRIC_TYPE = "rubric_type";
    public static final List<String> RUBRIC_TYPES = Arrays.asList("1xN", "NxN");

    public static final String FETCH_RUBRIC =
        "SELECT id, title, url, is_remote, description, categories, type, feedback_guidance, total_points, overall_feedback_required,"
            + " creator_id, modifier_id, original_creator_id, original_rubric_id, parent_rubric_id, publish_date, publish_status, metadata, taxonomy,"
            + " gut_codes, thumbnail, created_at, updated_at, tenant, tenant_root, visible_on_profile, is_deleted, creator_system FROM rubric"
            + " WHERE id = ?::uuid AND is_deleted = false";

    private static final List<String> INSERT_RUBRIC_ALLOWED_FIELDS =
        Arrays.asList(TITLE, DESCRIPTION, TYPE, METADATA, TAXONOMY, THUMBNAIL);

    private static final List<String> INSERT_RUBRIC_MANDATORY_FIELDS = Arrays.asList(TITLE, TYPE);

    public static final List<String> INSERT_RUBRIC_FORBIDDEN_FIELDS = Arrays.asList(ID, URL, IS_REMOTE, CATEGORIES,
        CREATED_AT, UPDATED_AT, CREATOR_ID, MODIFIER_ID, ORIGINAL_CREATOR_ID, ORIGINAL_RUBRIC_ID, PARENT_RUBRIC_ID,
        GUT_CODES, PUBLISH_DATE, IS_DELETED, VISIBLE_ON_PROFILE, TENANT, TENANT_ROOT);

    public static final List<String> FETCH_RUBRIC_FIELDS = Arrays.asList(ID, TITLE, URL, IS_REMOTE, DESCRIPTION,
        CATEGORIES, TYPE, FEEDBACK_GUIDANCE, TOTAL_POINTS, OVERALL_FEEDBACK_REQUIRED, CREATOR_ID, MODIFIER_ID,
        ORIGINAL_CREATOR_ID, ORIGINAL_RUBRIC_ID, PARENT_RUBRIC_ID, PUBLISH_DATE, PUBLISH_STATUS, METADATA, TAXONOMY,
        GUT_CODES, THUMBNAIL, CREATED_AT, UPDATED_AT, TENANT, TENANT_ROOT, VISIBLE_ON_PROFILE, IS_DELETED, CREATOR_SYSTEM);

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
        converterMap.put(CREATOR_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(MODIFIER_ID, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(TYPE, (fieldValue -> FieldConverter.convertFieldToNamedType(fieldValue, RUBRIC_TYPE)));
        converterMap.put(TENANT, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(TENANT_ROOT, (fieldValue -> FieldConverter.convertFieldToUuid((String) fieldValue)));
        converterMap.put(GUT_CODES, (fieldValue -> FieldConverter.convertFieldToTextArray((String) fieldValue)));

        return Collections.unmodifiableMap(converterMap);
    }

    private static Map<String, FieldValidator> initializeValidators() {
        Map<String, FieldValidator> validatorMap = new HashMap<>();
        validatorMap.put(ID, (FieldValidator::validateUuid));
        validatorMap.put(TITLE, (value) -> FieldValidator.validateString(value, 1000));
        validatorMap.put(DESCRIPTION, (value) -> FieldValidator.validateStringAllowNullOrEmpty(value, 20000));
        validatorMap.put(TYPE, (value) -> RUBRIC_TYPES.contains(value));
        validatorMap.put(METADATA, FieldValidator::validateJsonIfPresent);
        validatorMap.put(TAXONOMY, FieldValidator::validateJsonIfPresent);
        validatorMap.put(THUMBNAIL, (value) -> FieldValidator.validateStringAllowNullOrEmpty(value, 2000));
        validatorMap.put(VISIBLE_ON_PROFILE, FieldValidator::validateBooleanIfPresent);
        validatorMap.put(TENANT, (FieldValidator::validateUuid));
        validatorMap.put(TENANT_ROOT, (FieldValidator::validateUuid));
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

    public static FieldSelector createFieldSelector() {
        return new FieldSelector() {
            @Override
            public Set<String> allowedFields() {
                return Collections.unmodifiableSet(new HashSet<>(INSERT_RUBRIC_ALLOWED_FIELDS));
            }

            @Override
            public Set<String> mandatoryFields() {
                return Collections.unmodifiableSet(new HashSet<String>(INSERT_RUBRIC_MANDATORY_FIELDS));
            }
        };
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

    private void setFieldUsingConverter(String fieldName, Object fieldValue) {
        FieldConverter fc = converterRegistry.get(fieldName);
        if (fc != null) {
            this.set(fieldName, fc.convertField(fieldValue));
        } else {
            this.set(fieldName, fieldValue);
        }
    }
}
