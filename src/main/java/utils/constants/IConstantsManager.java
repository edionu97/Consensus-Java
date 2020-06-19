package utils.constants;

import java.util.Optional;

public interface IConstantsManager {

    /***
     * Get the value of a constant, casted to T
     * @param <T>: the type of the constant
     * @param constantName: the name of the constant
     * @return Optional.empty if the constant does not exist or optional of value if it exists
     */
    <T> Optional<T> getConstantValue(final String constantName);
}
