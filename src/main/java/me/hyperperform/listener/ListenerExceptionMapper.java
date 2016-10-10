package me.hyperperform.listener;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Allows for intercepting exceptions that occur through REST api. Intercepted exceptions can be handled appropriately.
 *
 * @author  CodusMaximus
 * @version 1.0
 * @since   2016/07/10
 */

@Provider
public class ListenerExceptionMapper implements ExceptionMapper<NotFoundException> {

    /**
     *
     * @param e Exception that was intercepted.
     * @return  Returns appropriate response code with regards to exception type. E.g NotFoundException returns 404.
     */
    public Response toResponse(NotFoundException e) {
        return Response.status(404).entity(e.toString()).build();
    }
}
