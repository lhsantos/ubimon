package org.ubimon.server.uos;

import java.util.Map;

import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService.ParameterType;
import org.unbiquitous.uos.core.messageEngine.messages.Call;

/**
 * Some utilities functions to work with uOS entities.
 * 
 * @author Luciano Santos
 */
public final class UosUtil {
	private UosUtil() {
	}

	/**
	 * Validates the presence of all mandatory parameters of service call on
	 * given driver.
	 * 
	 * @param call
	 *            The service call.
	 * @param driver
	 *            The driver.
	 * 
	 * @throws IllegalArgumentException
	 *             if the service is not found or a mandatory parameter is not
	 *             present.
	 */
	public static void validateParameters(Call call, UpDriver driver) {
		UpService service = findService(driver, call.getService());
		if (service == null)
			throw new IllegalArgumentException("service not found on driver");

		Map<String, ParameterType> params = service.getParameters();

		for (String param : params.keySet()) {
			if ((params.get(param) == ParameterType.MANDATORY) && (call.getParameter(param) == null))
				throw new IllegalArgumentException("parameter '" + param + "' not provided");
		}
	}

	private static UpService findService(UpDriver driver, String name) {
		for (UpService service : driver.getServices()) {
			if (service.getName().equalsIgnoreCase(name))
				return service;
		}
		return null;
	}

	/**
	 * Extracts the integer value of a service call's parameter, if existent and
	 * valid.
	 * 
	 * @param call
	 *            The call.
	 * @param param
	 *            The parameter name.
	 * @param required
	 *            If this parameter is required.
	 * 
	 * @return The extracted value.
	 * 
	 * @throws IllegalArgumentException
	 *             if the parameter is required and not present or is not a
	 *             valid integer or integer string representation.
	 */
	public static Integer extractInt(Call call, String param, boolean required) {
		Object value = call.getParameter(param);
		if (value != null) {
			if (value instanceof Number)
				return ((Number) value).intValue();
			if (value instanceof String)
				return Integer.parseInt((String) value);

			throw new IllegalArgumentException("invalid value for parameter '" + param + "'");
		} else {
			if (required)
				throw new IllegalArgumentException("parameter '" + param + "' not provided");
			return null;
		}
	}

	/**
	 * Extracts the integer value of a service call's parameter, if existent and
	 * valid.
	 * 
	 * @param call
	 *            The call.
	 * @param param
	 *            The parameter name.
	 * @param type
	 *            The uOS type of this parameter.
	 * 
	 * @return The extracted value.
	 * 
	 * @throws IllegalArgumentException
	 *             if the parameter is {@link ParameterType#MANDATORY} and not
	 *             present or is not a valid integer or integer string
	 *             representation.
	 */
	public static Integer extractInt(Call call, String param, ParameterType type) {
		return extractInt(call, param, type == ParameterType.MANDATORY);
	}

	/**
	 * Extracts the integer value of a service call's parameter, if existent and
	 * valid.
	 * 
	 * @param call
	 *            The call.
	 * @param param
	 *            The parameter name.
	 * 
	 * @return The extracted value.
	 * 
	 * @throws IllegalArgumentException
	 *             if the parameter is not present or is not a valid integer or
	 *             integer string representation.
	 */
	public static Integer extractInt(Call call, String param) {
		return extractInt(call, param, true);
	}

	/**
	 * Extracts the double value of a service call's parameter, if existent and
	 * valid.
	 * 
	 * @param call
	 *            The call.
	 * @param param
	 *            The parameter name.
	 * @param required
	 *            If this parameter is required.
	 * 
	 * @return The extracted value.
	 * 
	 * @throws IllegalArgumentException
	 *             if the parameter is required and not present or is not a
	 *             valid double or double string representation.
	 */
	public static Double extractDouble(Call call, String param, boolean required) {
		Object value = call.getParameter(param);
		if (value != null) {
			if (value instanceof Number)
				return ((Number) value).doubleValue();
			if (value instanceof String)
				return Double.parseDouble((String) value);

			throw new IllegalArgumentException("invalid value for parameter '" + param + "'");
		} else {
			if (required)
				throw new IllegalArgumentException("parameter '" + param + "' not provided");
			return null;
		}
	}

	/**
	 * Extracts the double value of a service call's parameter, if existent and
	 * valid.
	 * 
	 * @param call
	 *            The call.
	 * @param param
	 *            The parameter name.
	 * @param type
	 *            The uOS type of this parameter.
	 * 
	 * @return The extracted value.
	 * 
	 * @throws IllegalArgumentException
	 *             if the parameter is {@link ParameterType#MANDATORY} and not
	 *             present or is not a valid double or double string
	 *             representation.
	 */
	public static Double extractDouble(Call call, String param, ParameterType type) {
		return extractDouble(call, param, type == ParameterType.MANDATORY);
	}

	/**
	 * Extracts the double value of a service call's parameter, if existent and
	 * valid.
	 * 
	 * @param call
	 *            The call.
	 * @param param
	 *            The parameter name.
	 * 
	 * @return The extracted value.
	 * 
	 * @throws IllegalArgumentException
	 *             if the parameter is not present or is not a valid double or
	 *             double string representation.
	 */
	public static Double extractDouble(Call call, String param) {
		return extractDouble(call, param, true);
	}
}
