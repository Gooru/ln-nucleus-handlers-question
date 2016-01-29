package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ashish on 11/1/16.
 */
@Table("content")
public class AJEntityQuestion extends Model {

  private static final Logger LOGGER = LoggerFactory.getLogger(AJEntityQuestion.class);

  public static final String VALIDATE_EXISTS_NON_DELETED =
    "select id, creator_id, publish_date, collection_id, course_id from content where content_format = ?::content_format_type and id = ?::uuid and " +
      "is_deleted = ?";

  public static final String FETCH_QUESTION =
    "select id, title, short_title, publish_date, description, answer, metadata, taxonomy, depth_of_knowledge, hint_explanation_detail, thumbnail, " +
      "creator_id from content where content_format = ?::content_format_type and id = ?::uuid and is_deleted = ?";

  public static final String AUTH_FILTER = "id = ?::uuid and (owner_id = ?::uuid or collaborator ?? ?);";

  public static final String TABLE_COURSE = "course";
  public static final String TABLE_COLLECTION = "collection";
  public static final String QUESTION = "question";
  public static final String COURSE_ID = "course_id";
  public static final String CREATOR_ID = "creator_id";

  public static final List<String> FETCH_QUESTION_FIELDS = Arrays
    .asList("id", "title", "short_title", "publish_date", "description", "answer", "metadata", "taxonomy", "depth_of_knowledge",
      "hint_explanation_detail", "thumbnail", "creator_id");

  // What fields are allowed in request payload. Note this does not include the auto populate fields
  public static final List<String> INSERT_QUESTION_ALLOWED_FIELDS = Arrays
    .asList("title", "description", "short_title", "content_subformat", "answer", "metadata", "taxonomy", "depth_of_knowledge",
      "hint_explanation_detail", "thumbnail", "visible_on_profile");
  public static final List<String> UPDATE_QUESTION_ALLOWED_FIELDS = Arrays
    .asList("title", "description", "short_title", "answer", "metadata", "taxonomy", "depth_of_knowledge", "hint_explanation_detail", "thumbnail",
      "visible_on_profile");
  public static final List<String> UPDATE_QUESTION_FORBIDDEN_FIELDS = Arrays
    .asList("id", "url", "created_at", "updated_at", "creator_id", "modifier_id", "original_creator_id", "original_content_id", "publish_date",
      "narration", "content_format", "content_subformat", "course_id", "unit_id", "lesson_id", "collection_id", "sequence_id", "is_copyright_owner",
      "copyright_owner", "info", "display_guide", "accessibility", "is_deleted");
  public static final List<String> INSERT_QUESTION_FORBIDDEN_FIELDS = Arrays
    .asList("id", "url", "created_at", "updated_at", "creator_id", "modifier_id", "original_creator_id", "original_content_id", "publish_date",
      "narration", "content_format", "course_id", "unit_id", "lesson_id", "collection_id", "sequence_id", "is_copyright_owner", "copyright_owner",
      "info", "display_guide", "accessibility", "is_deleted");

  public static final String COLLECTION_ID = "collection_id";
  public static final String MODIFIER_ID = "modifier_id";

  public static final String UUID_TYPE = "uuid";
  public static final String JSONB_TYPE = "jsonb";
  public static final String IS_DELETED = "is_deleted";

  public void setModifierId(String modifier) {
    setPGObject(this.MODIFIER_ID, this.UUID_TYPE, modifier);
  }

  public void setCreatorId(String creatorId) {
    setPGObject(this.CREATOR_ID, this.UUID_TYPE, creatorId);
  }

  // NOTE:
  // We do not deal with nested objects, only first level ones
  // We do not check for forbidden fields, it should be done before this
  public void setAllFromJson(JsonObject input) {
    input.getMap().forEach((s, o) -> {
      // Note that special UUID cases for modifier and creator should be handled internally and not via map, so we do not care
      if (o instanceof JsonObject) {
        this.setPGObject(s, JSONB_TYPE, ((JsonObject) o).toString());
      } else if (o instanceof JsonArray) {
        this.setPGObject(s, JSONB_TYPE, ((JsonArray) o).toString());
      } else {
        this.set(s, o);
      }
    });
  }

  private void setPGObject(String field, String type, String value) {
    PGobject pgObject = new PGobject();
    pgObject.setType(type);
    try {
      pgObject.setValue(value);
      this.set(field, pgObject);
    } catch (SQLException e) {
      LOGGER.error("Not able to set value for field: {}, type: {}, value: {}", field, type, value);
      this.errors().put(field, value);
    }
  }
}
