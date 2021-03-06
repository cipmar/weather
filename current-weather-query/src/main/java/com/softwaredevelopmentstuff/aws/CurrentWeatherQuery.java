package com.softwaredevelopmentstuff.aws;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwaredevelopmentstuff.aws.utils.CustomLogFormatter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public class CurrentWeatherQuery {
    private static final Logger LOGGER = Logger.getLogger("weather-query");

    static {
        LOGGER.setUseParentHandlers(false);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new CustomLogFormatter());
        LOGGER.addHandler(consoleHandler);
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int DAYS_BACK = 3;

    public void handler(InputStream inputStream, OutputStream outputStream) throws IOException {
        LOGGER.info("Enter method");
        QueryRequest queryRequest = OBJECT_MAPPER.readValue(inputStream, QueryRequest.class);

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDBMapper mapper = new DynamoDBMapper(client);

        Condition rangeKeyCondition = new Condition();
        rangeKeyCondition
                .withComparisonOperator(ComparisonOperator.GE)
                .withAttributeValueList(new AttributeValue().withN(String.valueOf(getTimestamp(DAYS_BACK))));

        DynamoDBQueryExpression<WeatherData> query = new DynamoDBQueryExpression<WeatherData>()
                .withHashKeyValues(new WeatherData(queryRequest.getCityId()))
                .withRangeKeyCondition("timestamp", rangeKeyCondition);

        LOGGER.info("Query built, executing...");
        List<WeatherData> weatherDataList = mapper.query(WeatherData.class, query);
        LOGGER.info("Query executed");

        OBJECT_MAPPER.writeValue(outputStream, weatherDataList);
        LOGGER.info("Response written");
    }

    private Long getTimestamp(int daysBack) {
        return (System.currentTimeMillis() - daysBack * 24 * 60 * 60 * 1000) / 1000;
    }
}
