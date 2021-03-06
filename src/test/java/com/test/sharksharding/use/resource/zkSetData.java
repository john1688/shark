package com.test.sharksharding.use.resource;

/*
 * Copyright 2015-2101 gaoxianglong
 *
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
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.CountDownLatch;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 向zookeeper配置中心添加数据
 * 
 * @author gaoxianglong
 * 
 * @version 1.3.7
 */
public class zkSetData {
	private Logger logger = LoggerFactory.getLogger(zkSetData.class);

	public @Test void testSetData() {
		try (BufferedReader reader = new BufferedReader(new FileReader("c:/shark-datasource.xml"))) {
			StringBuffer str = new StringBuffer();
			String value = "";
			while (null != (value = reader.readLine()))
				str.append(value);
			final CountDownLatch countDownLatch = new CountDownLatch(1);
			ZooKeeper zk_client = new ZooKeeper(
					"120.25.58.116:2181,120.25.58.116:2182,120.25.58.116:2183,120.25.58.116:2184", 30000,
					new Watcher() {
						@Override
						public void process(WatchedEvent event) {
							final KeeperState STATE = event.getState();
							switch (STATE) {
							case SyncConnected:
								countDownLatch.countDown();
								logger.info("成功连接zookeeper服务器");
								break;
							case Disconnected:
								logger.warn("与zookeeper服务器断开连接");
								break;
							case Expired:
								logger.error("session会话失效...");
								break;
							case AuthFailed:
								logger.error("ACL认证失败...");
							default:
								break;
							}
						}
					});
			countDownLatch.await();
			zk_client.setData("/info/datasource", str.toString().getBytes(), -1);
			logger.info("insert success");
		} catch (Exception e) {
			logger.error("insert fail", e);
		}
	}
}