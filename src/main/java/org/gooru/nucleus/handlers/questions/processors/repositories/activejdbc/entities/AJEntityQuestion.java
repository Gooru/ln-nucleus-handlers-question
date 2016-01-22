package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ashish on 11/1/16.
 */
@Table("content")
public class AJEntityQuestion extends Model {

  public static final String VALIDATE_EXISTS_NON_DELETED =
    "select id, creator_id, publish_date from content where content_format = ?::content_format_type and id = ? and is_deleted = ?";

  public static final String FETCH_QUESTION =
    "select id, title, short_title, publish_date, description, answer, metadata, taxonomy, depth_of_knowledge, hint_explanation_detail, thumbnail, " +
      "creator_id from content where content_format = ?::content_format_type and id = ? and is_deleted = ?";

  public static final String QUESTION = "question";

  public static final List<String> FETCH_QUESTION_FIELDS = Arrays.asList("id", "title", "short_title", "publish_date", "description", "answer", "metadata", "taxonomy", "depth_of_knowledge", "hint_explanation_detail", "thumbnail", "creator_id");

}
