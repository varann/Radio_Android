package org.gigahub.radio.android.api;

import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Rest;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;

import java.util.List;

/**
 * Created by asavinova on 03/10/14.
 */
@Rest(rootUrl = "http://radio-hub.appspot.com/api/1.0", converters = { MappingJacksonHttpMessageConverter.class })
public interface RestApiClient {

    @Get("/stations")
    List<Station> getStations();

}
