package judgels.uriel.api.contest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import judgels.persistence.api.Page;

@Path("/api/v2/contests")
public interface ContestService {
    @GET
    @Path("/{contestJid}")
    @Produces(APPLICATION_JSON)
    Contest getContest(@PathParam("contestJid") String contestJid);

    @GET
    @Path("/")
    @Produces(APPLICATION_JSON)
    Page<Contest> getContests(
            @DefaultValue("1") @QueryParam("page") int page,
            @DefaultValue("10") @QueryParam("pageSize") int pageSize);

    @POST
    @Path("/")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    Contest createContest(ContestData contestData);

}
