package com.netapp.test;

import org.apache.flink.cdc.connectors.sqlserver.SqlServerSource;
import org.apache.flink.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

public class SqlServerSourceExample {

    public static void main(String[] args) throws Exception {
        SourceFunction<String> sourceFunction = SqlServerSource.<String>builder()
                .hostname("localhost")
                .port(1433)
                .database("sqlserver") // monitor sqlserver database
                .tableList("dbo.products") // monitor products table
                .username("sa")
                .password("Password!")
                .deserializer(new JsonDebeziumDeserializationSchema()) // converts SourceRecord to JSON String
                .build();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.addSource(sourceFunction)
                .print().setParallelism(1); // use parallelism 1 for sink to keep message ordering

        env.execute();
    }
}

