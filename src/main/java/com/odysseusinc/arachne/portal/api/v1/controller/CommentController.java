/*
 *
 * Copyright 2018 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: April 20, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.NO_ERROR;

import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.portal.api.v1.dto.CommentDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.CommentTopicDTO;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.model.Comment;
import com.odysseusinc.arachne.portal.model.CommentTopic;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.service.CommentService;
import io.swagger.annotations.ApiOperation;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommentController extends BaseController {

    private final CommentService commentService;
    private final GenericConversionService conversionService;

    @Autowired
    public CommentController(CommentService commentService,
                             GenericConversionService conversionService) {

        this.commentService = commentService;
        this.conversionService = conversionService;
    }

    @ApiOperation(value = "Get topic with all comments")
    @RequestMapping(value = "/api/v1/comments/{topicId}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            method = RequestMethod.GET)
    public JsonResult<CommentTopicDTO> get(@PathVariable("topicId") Long topicId) throws NotExistException {

        final CommentTopic topic = commentService.getTopic(topicId);
        JsonResult<CommentTopicDTO> result = new JsonResult<>(NO_ERROR);
        result.setResult(conversionService.convert(topic, CommentTopicDTO.class));
        return result;
    }

    @ApiOperation(value = "Add new comment")
    @RequestMapping(value = "/api/v1/comments/{topicId}", method = RequestMethod.POST)
    public JsonResult<CommentDTO> addComment(@PathVariable("topicId") Long topicId,
                                             @Validated @RequestBody CommentDTO commentDTO,
                                             Principal principal
    ) throws PermissionDeniedException {

        final IUser user = getUser(principal);
        final Comment comment = conversionService.convert(commentDTO, Comment.class);
        comment.setAuthor(user);
        final Comment saved = commentService.addComment(topicId, commentDTO.getParentId(), comment);
        JsonResult<CommentDTO> result = new JsonResult<>(NO_ERROR);
        result.setResult(conversionService.convert(saved, CommentDTO.class));
        return result;
    }

    @ApiOperation(value = "Delete comment")
    @RequestMapping(value = "/api/v1/comments/{topicId}/{commentId}", method = RequestMethod.DELETE)
    public JsonResult deleteComment(@PathVariable("topicId") Long topicId,
                                    @PathVariable("commentId") Long commentId
    ) throws NotExistException {

        commentService.deleteComment(topicId, commentId);
        return new JsonResult(NO_ERROR);
    }
}
