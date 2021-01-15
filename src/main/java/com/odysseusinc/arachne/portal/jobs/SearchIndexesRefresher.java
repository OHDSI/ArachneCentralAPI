/*
 *
 * Copyright 2020 Odysseus Data Services, inc.
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
 * Authors: Alexandr Cumarav, Vitaly Koulakov, Yaroslav Molodkov
 * Created: November 04, 2020
 */

package com.odysseusinc.arachne.portal.jobs;

import com.odysseusinc.arachne.portal.exception.ArachneSystemRuntimeException;
import com.odysseusinc.arachne.portal.model.AbstractUserStudyListItem;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.Skill;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.search.PaperSearch;
import com.odysseusinc.arachne.portal.model.search.StudySearch;
import com.odysseusinc.arachne.portal.service.BaseDataSourceService;
import com.odysseusinc.arachne.portal.service.BasePaperService;
import com.odysseusinc.arachne.portal.service.BaseStudyService;
import com.odysseusinc.arachne.portal.service.BaseUserService;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;


@Component
public class SearchIndexesRefresher<IDS extends IDataSource,
        IUS extends IUser,
        IStudy extends Study,
        IStudySearch extends StudySearch,
        IUserStudyListItem extends AbstractUserStudyListItem,
        IAnalysis extends Analysis,
        IPaper extends Paper,
        IPaperSearch extends PaperSearch> {

    private static final Logger log = LoggerFactory.getLogger(SearchIndexesRefresher.class);

    private final BaseAnalysisService<IAnalysis> analysisService;
    private final BaseDataSourceService<IDS> dataSourceService;
    private final BasePaperService<IPaper, IPaperSearch, IStudy, IDS, IStudySearch, IUserStudyListItem> paperService;
    private final BaseStudyService<IStudy, IDS, IStudySearch, IUserStudyListItem> studyService;
    private final BaseUserService<IUS, Skill> userService;

    @Autowired
    public SearchIndexesRefresher(BaseDataSourceService<IDS> dataSourceService, BaseUserService<IUS, Skill> userService, BaseStudyService<IStudy, IDS, IStudySearch, IUserStudyListItem> studyService, BaseAnalysisService<IAnalysis> analysisService, BasePaperService<IPaper, IPaperSearch, IStudy, IDS, IStudySearch, IUserStudyListItem> paperService) {

        this.dataSourceService = dataSourceService;
        this.userService = userService;
        this.studyService = studyService;
        this.analysisService = analysisService;
        this.paperService = paperService;
    }

    @EventListener(classes = ContextRefreshedEvent.class)
    @Transactional
    public void startIndicesRebuild() {

        log.info("Start indices rebuilding...");
        CompletableFuture.runAsync(this::rebuildAllIndexes)
                .thenRun(() -> log.info("Search indices rebuild complete"))
                .exceptionally(err -> {
                    log.warn("Cannot rebuild search indices due to the error:", err);
                    return null;
                });

    }

    private void rebuildAllIndexes() {

        try {
            dataSourceService.indexAllBySolr();
            userService.indexAllBySolr();
            studyService.indexAllBySolr();
            analysisService.indexAllBySolr();
            paperService.indexAllBySolr();
        } catch (Exception ex) {
            throw new ArachneSystemRuntimeException(ex);
        }

    }
}