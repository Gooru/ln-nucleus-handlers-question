package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * Created by ashish on 11/1/16.
 */
@Table("content")
public class AJEntityQuestion extends Model {

  public static final String VALIDATE_EXISTS_NON_DELETED =
    "select id, creator_id, publish_date from content where content_format = ?::content_format_type and id = ? and is_deleted = ?";

  public static final String QUESTION = "question";

}
