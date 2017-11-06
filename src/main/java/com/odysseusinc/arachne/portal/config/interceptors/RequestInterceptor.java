/*
 *  Copyright 2017 Observational Health Data Sciences and Informatics
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Company: Odysseus Data Services, Inc.
 *  Product Owner/Architecture: Gregory Klebanov
 *  Authors: Anton Gackovka
 *  Created: November 6, 2017
 *
 */

package com.odysseusinc.arachne.portal.config.interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RequestInterceptor implements AsyncHandlerInterceptor {

	private ThreadLocal<Long> time = new ThreadLocal<>();

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestInterceptor.class);

	@Autowired
	private QueryInterceptor interceptor;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		time.set(System.currentTimeMillis());
		interceptor.startCounter();
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

		long duration = System.currentTimeMillis() - time.get();
		Long queryCount = interceptor.getCount();
		interceptor.clearCounter();
		time.remove();
		LOGGER.info("[Time: {} ms] [Queries: {}] {} {}", duration, queryCount, request.getMethod(), request.getRequestURI());
	}

	@Override
	public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		interceptor.clearCounter();
		time.remove();
	}

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }
}


