package utils.constants.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import utils.constants.IConstantsManager;
import utils.constants.model.Constants;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConstantsManager implements IConstantsManager {

    private final Map<String, Object> constantsMap = new HashMap<>();

    public ConstantsManager() {
        //parse the json to JAVA object, and populate the constants map
        try {
            populateConstantsMap(new ObjectMapper().readValue(new File("src/main/resources/constants.json"), Constants.class));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getConstantValue(final String constantName) {
        if(constantsMap.containsKey(constantName)) {
            return (Optional<T>) Optional.of(constantsMap.get(constantName));
        }
        return Optional.empty();
    }

    /**
     * Create an association between property name and property value
     * @param constants: the parsed POJO
     * @throws Exception: if something is wrong
     */
    private void populateConstantsMap(final Constants constants) throws Exception {
        //get through all the fields
        for (var field : constants.getClass().getDeclaredFields()) {
            //get the value of the constants (name and value)
            final String fieldName = field.getName();
            final var fieldValue = new PropertyDescriptor(
                    fieldName,
                    Constants.class
            ).getReadMethod().invoke(constants);

            //put the value into map
            constantsMap.put(fieldName, fieldValue);
        }
    }
}
