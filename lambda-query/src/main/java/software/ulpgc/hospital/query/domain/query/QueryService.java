package software.ulpgc.hospital.query.domain.query;

public interface QueryService {
    QueryResult getEventById(String eventId) throws QueryException;
    QueryResult getAllEvents() throws QueryException;
}
