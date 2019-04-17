// IHermesService.aidl
package org.hermes.service;


interface IHermesService {

    /**
     * Give all the metadata of each package and get back the UUID
     */
    String register(String dataId, String unit, String mtype, String what,
                    String device);

    String deregister(String uuid);

    /**
     * Use this call to send any data that has numeric values.
     * If the rate of data arrival is faster than the rate of data broadcast they will be downsampled.
     * If the delivery has been successful, an empty string will be returned.
     * For an enumeration of errors, look in the implementation of this interface.
     */
    String sendDataDouble(String uuid, double value, String http_method,
                          String http_code, String result, String stat, String direction,
                          String file, int line, String env);

    /**
     * Use this call to send any data that has non-numeric samples.
     * If the rate of data arrival is faster than the rate of data broadcast they will not be downsamples
     * but they will be thrown away.
     * If the delivery has been successful, an empty string will be returned.
     * For an enumeration of errors, look in the implementation of this interface.
     */
    String sendDataString(String uuid, String value, String http_method,
                          String http_code, String result, String stat, String direction,
                          String file, int line, String env);
}
