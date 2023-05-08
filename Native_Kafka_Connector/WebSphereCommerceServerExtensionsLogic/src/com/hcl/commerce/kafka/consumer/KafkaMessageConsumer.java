package com.hcl.commerce.kafka.consumer;

import com.ibm.commerce.cf.exception.FoundationMessageKey;
import com.ibm.commerce.cf.kafka.KafkaConsumerConfig;
import com.ibm.commerce.cf.kafka.util.ExponentialBackoff;
import com.ibm.commerce.cf.kafka.util.KafkaStatusHelper;
import com.ibm.commerce.cf.logging.LoggerHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

public class KafkaMessageConsumer implements Runnable{
	public static final String COPYRIGHT = "(c) Copyright International Business Machines Corporation 1996,2008";
	private static final String CLASS_NAME = KafkaMessageConsumer.class.getName();
	private static final Logger LOGGER = LoggerHelper.getInstance().getLogger(KafkaMessageConsumer.class);
	private static final String BOOTSTRAP_SERVERS = "bootstrap.servers";
	private static final long DEFAULT_MAX_POLL_TIME_MS = 300000L;
	private Properties config;
	private AtomicBoolean closed;
	private KafkaConsumer<String, String> consumer;
	private List<String> topicList;
	private long pollTimeOut;
	private boolean enableSeekToEnd;
	private ExponentialBackoff recoveryBackOff;
	private List<ConsumerRecord<String, String>> record;

	public KafkaMessageConsumer(Properties config, String topicName) {
		this.closed = new AtomicBoolean(false);
		this.pollTimeOut = 10000L;
		this.enableSeekToEnd = false;
		this.recoveryBackOff = new ExponentialBackoff(2.0D, 1000L, 60000L);
		this.config = config;
		this.setTopic(topicName);
	}

	public KafkaMessageConsumer(boolean enableTopicPrefix, String topic) {
		this.closed = new AtomicBoolean(false);
		this.pollTimeOut = 10000L;
		this.enableSeekToEnd = false;
		this.recoveryBackOff = new ExponentialBackoff(2.0D, 1000L, 60000L);
		String methodName = "MessageConsumer(boolean enbleTopicPrefix, String topic)";
		boolean isLoggable = LOGGER.isLoggable(Level.FINER);
		if (isLoggable) {
			LOGGER.entering(CLASS_NAME, "MessageConsumer(boolean enbleTopicPrefix, String topic)",
					new Object[] { enableTopicPrefix, topic });
		}

		KafkaConsumerConfig config = new KafkaConsumerConfig();
		this.setKafkaConfiguration(config);
		if (enableTopicPrefix) {
			this.setTopic(config.getTopicPrefix() + "-" + topic);
		} else {
			this.setTopic(topic);
		}

		if (isLoggable) {
			LOGGER.exiting(CLASS_NAME, "MessageConsumer(boolean enbleTopicPrefix, String topic)");
		}

	}

	public KafkaMessageConsumer(String topic) {
		this(false, topic);
	}

	public String getTopic() {
		return this.topicList != null && !this.topicList.isEmpty() ? (String) this.topicList.get(0) : null;
	}

	public void setKafkaConfiguration(Properties config) {
		this.config = config;
		if (this.config.getProperty("bootstrap.servers") == null) {
			this.config = null;
		}

	}

	public void setKafkaConfiguration(KafkaConsumerConfig config) {
		this.setKafkaConfiguration(config.getConfigProperties());
	}

	public void setTopic(String topic) {
		this.topicList = Arrays.asList(topic);
	}

	public void enableSeekToEnd() {
		this.enableSeekToEnd = true;
	}

	public void run() {
		String methodName = "start()";
		boolean isLoggable = LOGGER.isLoggable(Level.FINER);
		if (isLoggable) {
			LOGGER.entering(CLASS_NAME, "start()", new Object[] { this.topicList });
		}
		if (this.config != null) {
			long maxPollTime = this.getMaxPollTime();

			boolean recover;
			do {
				recover = false;
				this.consumer = new KafkaConsumer(this.config);

				try {
					this.consumer.subscribe(this.topicList);
					while (!this.closed.get()) {
						ConsumerRecords<String, String> records = this.consumer.poll(this.pollTimeOut);
						List<ConsumerRecord<String, String>> consumerRecordsList = new ArrayList<>();
						if (this.enableSeekToEnd) {
							this.consumer.seekToEnd(records.partitions());
						}
						Iterator var7 = records.partitions().iterator();

						while (var7.hasNext()) {
							TopicPartition partition = (TopicPartition) var7.next();
							List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
							long lastOffset = ((ConsumerRecord) partitionRecords.get(partitionRecords.size() - 1))
									.offset();
							this.consumer.commitSync(
									Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1L)));
							long startTime = System.currentTimeMillis();
							Iterator var14 = partitionRecords.iterator();

							while (var14.hasNext()) {
								ConsumerRecord<String, String> record = (ConsumerRecord) var14.next();
								consumerRecordsList.add(record);
								this.setRecord(consumerRecordsList);
							}

							long endTime = System.currentTimeMillis();
							long spentTime = endTime - startTime;
							if (spentTime > maxPollTime) {
								LOGGER.logp(Level.WARNING, CLASS_NAME, "run()",
										"Time spent: " + (endTime - startTime) + ", Record processed:"
												+ partitionRecords.size() + ", Session Time Out:" + maxPollTime);
							} else if (isLoggable) {
								LOGGER.logp(Level.FINER, CLASS_NAME, "run()",
										"Time spent: " + (endTime - startTime) + ", Record processed:"
												+ partitionRecords.size() + ", Session Time Out:" + maxPollTime);
							}
						}

						this.closed.set(true);
						this.recoveryBackOff.reset();
						this.consumer.close();
					}
				} catch (Exception var23) {
					if (!this.closed.get()) {
						LOGGER.logp(Level.SEVERE, CLASS_NAME, "run()",
								FoundationMessageKey._ERR_KAFKA_POLL_TOPIC.getMessage((Locale) null,
										new Object[] { this.topicList, ExceptionUtils.getStackTrace(var23) }));
						recover = true;
						this.closed.set(false);
					}
				} finally {
					this.consumer.close();
				}

				if (recover && !this.closed.get()) {
					this.recoveryBackOff.nextDuration();
				}
			} while (recover && !this.closed.get());

			KafkaStatusHelper.setServerShutDown(true);
		}

		if (isLoggable) {
			LOGGER.exiting(CLASS_NAME, "run()");
		}

	}

	private Long getMaxPollTime() {
		String maxPollTimeProperty = this.config.getProperty("max.poll.interval.ms");
		return maxPollTimeProperty != null ? Long.parseLong(maxPollTimeProperty) : 300000L;
	}

	public long getPollTimeOut() {
		return this.pollTimeOut;
	}

	public void setPollTimeOut(long pollTimeOut) {
		this.pollTimeOut = pollTimeOut;
	}

	public void shutdown() {
		String methodName = "shutdown()";
		boolean isLoggable = LOGGER.isLoggable(Level.FINER);
		if (isLoggable) {
			LOGGER.entering(CLASS_NAME, "shutdown()");
		}

		KafkaStatusHelper.setServerShutDown(true);
		this.closed.set(true);
		if (this.consumer != null) {
			this.consumer.wakeup();
		}

		if (isLoggable) {
			LOGGER.exiting(CLASS_NAME, "shutdown()");
		}
	}

	public List<ConsumerRecord<String, String>> getRecord() {
		return record;
	}

	public void setRecord(List<ConsumerRecord<String, String>> record) {
		this.record = record;
	}

}
