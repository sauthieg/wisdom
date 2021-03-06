/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wisdom.monitor.extensions.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Parameter;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.annotations.View;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;
import org.wisdom.api.security.Authenticated;
import org.wisdom.api.templates.Template;
import org.wisdom.monitor.extensions.security.MonitorAuthenticator;
import org.wisdom.monitor.service.MonitorExtension;

import java.util.ArrayList;
import java.util.List;

@Controller
@Authenticated("Monitor-Authenticator")
public class LoggerExtension extends DefaultController implements MonitorExtension {

    @View("monitor/loggers")
    Template template;

    @Route(method = HttpMethod.GET, uri = "/monitor/logs")
    public Result index() {
        return ok(render(template));
    }


    @Route(method = HttpMethod.GET, uri = "/monitor/logs/loggers")
    public Result loggers() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        List<LoggerModel> loggers = new ArrayList<>();
        for (Logger logger : context.getLoggerList()) {
            loggers.add(new LoggerModel(logger));
        }
        return ok(loggers).json();
    }


    @Route(method = HttpMethod.PUT, uri = "/monitor/logs/{name}")
    public Result setLevel(@Parameter("name") String loggerName, @Parameter("level") String level) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger(loggerName);
        if (logger != null) {
            logger().info("Setting the log level of {} to {}", loggerName, level);
            ((ch.qos.logback.classic.Logger) logger).setLevel(Level.toLevel(level));
            return loggers();
        } else {
            logger().warn("Cannot set the level of logger {} - the logger does not exist", loggerName);
            return notFound();
        }
    }

    @Override
    public String label() {
        return "Loggers";
    }

    @Override
    public String url() {
        return "/monitor/logs";
    }

    @Override
    public String category() {
        return "Wisdom";
    }
}
