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
package org.wisdom.akka.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.wisdom.api.annotations.scheduler.Every;
import org.wisdom.api.scheduler.Scheduled;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Checks the Akka Scheduler.
 */
public class AkkaSchedulerTest {

    AkkaScheduler scheduler = new AkkaScheduler();

    @Before
    public void setUp() throws ClassNotFoundException {
        BundleContext context = mock(BundleContext.class);
        Bundle bundle = mock(Bundle.class);
        when(bundle.getBundleId()).thenReturn(1L);
        when(context.getBundle()).thenReturn(bundle);
        doAnswer(
                new Answer<Class>() {
                    @Override
                    public Class answer(InvocationOnMock invocation) throws Throwable {
                        return AkkaSchedulerTest.class.getClassLoader().loadClass((String) invocation.getArguments()[0]);
                    }
                }
        ).when(bundle).loadClass(anyString());
        final AkkaBootstrap akka = new AkkaBootstrap(context);
        akka.start();
        scheduler.akka = akka;
    }

    @After
    public void tearDown() {
        ((AkkaBootstrap) scheduler.akka).stop();
    }

    @Test
    public void testScheduled() throws InterruptedException {
        MyScheduled scheduled = new MyScheduled();
        scheduler.bindScheduled(scheduled);
        assertThat(scheduler.jobs).hasSize(1);
        Thread.sleep(2000);
        assertThat(scheduled.counter.get()).isGreaterThan(0);
        scheduler.unbindScheduled(scheduled);
        assertThat(scheduler.jobs).hasSize(0);
    }

    private class MyScheduled implements Scheduled {

        AtomicInteger counter = new AtomicInteger();


        @Every("1s")
        public void operation() {
            counter.incrementAndGet();
            System.out.println("Operate...");
        }
    }

}
