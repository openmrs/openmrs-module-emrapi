/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.emrapi.adt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Visit;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.queue.api.QueueEntryService;
import org.openmrs.module.queue.api.search.QueueEntrySearchCriteria;
import org.openmrs.module.queue.model.QueueEntry;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * If the queue module is loaded, this will ensure all queue entries are ended on the visit stop date
 * prior to attempting to save the visit, when performing a close visit operation
 */
@OpenmrsProfile(modules = { "queue:*" })
public class EndQueueEntriesVisitAction implements CloseVisitAction {

    private final Log log = LogFactory.getLog(getClass());

    private final QueueEntryService queueEntryService;

    @Autowired
    public EndQueueEntriesVisitAction(QueueEntryService queueEntryService) {
        this.queueEntryService = queueEntryService;
    }

    public void beforeSaveVisit(Visit visit) {
        if (visit.getStopDatetime() != null) {
            QueueEntrySearchCriteria criteria = new QueueEntrySearchCriteria();
            criteria.setVisit(visit);
            criteria.setIsEnded(false);
            List<QueueEntry> queueEntries = queueEntryService.getQueueEntries(criteria);
            if (!queueEntries.isEmpty()) {
                log.debug("Closing " + +queueEntries.size() + " queue entries prior to saving visit");
            }
            for (QueueEntry qe : queueEntries) {
                qe.setEndedAt(visit.getStopDatetime());
                queueEntryService.saveQueueEntry(qe);
                log.trace("Closed queue entry " + qe + " on " + visit.getStopDatetime());
            }
        }
    }
}
