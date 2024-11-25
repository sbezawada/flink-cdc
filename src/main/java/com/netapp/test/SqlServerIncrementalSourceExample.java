package com.netapp.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.netapp.test.data.JsonObject;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.cdc.connectors.base.options.StartupOptions;
import org.apache.flink.cdc.connectors.sqlserver.source.SqlServerSourceBuilder;
import org.apache.flink.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSink;

public class SqlServerIncrementalSourceExample {

    public static void main(String[] args) {

        try {
            SqlServerSourceBuilder.SqlServerIncrementalSource<String> sqlServerSource =
                    new SqlServerSourceBuilder<String>()
                            .hostname("localhost")
                            .port(1433)
                            .databaseList("inventory")
                            .tableList("dbo.products")
                            .username("sa")
                            .password("Password!")
                            .deserializer(new JsonDebeziumDeserializationSchema())
                            .startupOptions(StartupOptions.latest())
                            .build();

            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            // enable checkpoint
            env.enableCheckpointing(3000);
            // set the source parallelism to 2
            DataStream<String> cdcSourceStream = env.fromSource(
                            sqlServerSource,
                            WatermarkStrategy.noWatermarks(),
                            "SqlServerIncrementalSource")
                    .setParallelism(1);

            DataStream<JsonObject> objectStream = cdcSourceStream.map(new JsonObjectMapFunction());

//            PrintSink<JsonObject> sink = new PrintSink<>(true);
//            objectStream.sinkTo(sink);

            objectStream.addSink(JdbcSink.sink(
                    "insert into dbo.products (name, description, weight) values (?, ?, ?) ",
                    (statement, jsonObject) -> {
                        statement.setString(1, jsonObject.after.name);
                        statement.setString(2, jsonObject.after.description);
                        statement.setFloat(3, (float) jsonObject.after.weight);
                    },
                    JdbcExecutionOptions.builder()
                            .withBatchSize(1000)
                            .withBatchIntervalMs(200)
                            .withMaxRetries(5)
                            .build(),
                    new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                            .withUrl("jdbc:sqlserver://localhost:1433;databaseName=orders;")
                            .withDriverName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
                            .withUsername("sa")
                            .withPassword("Password!")
                            .build()
            ));
            env.execute("Print SqlServer Snapshot + Change Stream");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class JsonObjectMapFunction implements MapFunction<String, JsonObject> {

        @Override
        public JsonObject map(String rawJson) throws Exception {
            ObjectMapper mapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addDeserializer(JsonObject.class, new CustomDeserializer());
            mapper.registerModule(module);
            var jsonObject = mapper.readValue(rawJson, JsonObject.class);
            ;
            return jsonObject;
        }
    }
}