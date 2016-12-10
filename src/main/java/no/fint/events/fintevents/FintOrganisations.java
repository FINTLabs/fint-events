package no.fint.events.fintevents;

import lombok.extern.slf4j.Slf4j;
import no.fint.events.Events;
import no.fint.events.properties.EventsProps;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class FintOrganisations {

    @Autowired
    private EventsProps eventsProps;

    @Autowired
    private Events events;

    private List<FintOrganisation> organisations;

    @PostConstruct
    public void init() {
        organisations = getDefaultQueues();
        if (!eventsProps.isTestMode()) {
            organisations.forEach(fintOrganisation -> {
                log.info("Setting up queue for: {}", fintOrganisation.getName());
                events.addQueues(fintOrganisation.getExchange(),
                        fintOrganisation.getDownstreamQueue(),
                        fintOrganisation.getUpstreamQueue(),
                        fintOrganisation.getUndeliveredQueue());
            });
        }
    }

    public void addOrganization(String orgId) {
        FintOrganisation fintOrganisation = new FintOrganisation(
                orgId,
                eventsProps.getDefaultDownstreamQueue(),
                eventsProps.getDefaultUpstreamQueue(),
                eventsProps.getDefaultUndeliveredQueue()
        );

        organisations.add(fintOrganisation);
        events.addQueues(
                fintOrganisation.getExchange(),
                fintOrganisation.getDownstreamQueue(),
                fintOrganisation.getUpstreamQueue(),
                fintOrganisation.getUndeliveredQueue()
        );
    }

    public void removeOrganization(String orgId) {
        Optional<FintOrganisation> organization = get(orgId);
        if (organization.isPresent()) {
            FintOrganisation org = organization.get();
            events.deleteQueues(
                    org.getExchange(),
                    org.getDownstreamQueue(),
                    org.getUpstreamQueue(),
                    org.getUndeliveredQueue()
            );

            Optional<Integer> index = getIndex(orgId);
            index.ifPresent(integer -> organisations.remove(integer.intValue()));
        } else {
            log.error("Organization {} not found", orgId);
        }
    }

    public List<String> getRegisteredOrgIds() {
        return organisations.stream().map(FintOrganisation::getName).collect(Collectors.toList());
    }

    public boolean containsOrganisation(String orgId) {
        return get(orgId).isPresent();
    }

    public void deleteDefaultQueues() {
        getDefaultQueues().forEach(org -> events.deleteQueues(
                org.getExchange(),
                org.getDownstreamQueue(),
                org.getUpstreamQueue(),
                org.getUndeliveredQueue()));
    }

    private List<FintOrganisation> getDefaultQueues() {
        return Arrays.stream(eventsProps.getOrganizations()).map(org -> new FintOrganisation(
                org,
                eventsProps.getDefaultDownstreamQueue(),
                eventsProps.getDefaultUpstreamQueue(),
                eventsProps.getDefaultUndeliveredQueue()))
                .collect(Collectors.toList());
    }

    public Optional<FintOrganisation> get(String orgId) {
        return organisations.stream().filter(org -> org.getName().equals(orgId)).findAny();
    }

    public List<FintOrganisation> getAll() {
        return organisations;
    }

    private Optional<Integer> getIndex(String orgId) {
        for (int i = 0; i < organisations.size(); i++) {
            FintOrganisation o = organisations.get(i);
            if (o.getName().equals(orgId)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

}
