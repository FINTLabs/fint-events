package no.fint.events.model;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Event")
public class Event<T> implements Serializable {
    @XmlElement(required = true)
    private String corrId;

    @XmlElement(required = true)
    private String verb;

    @XmlElement(nillable = true, required = true)
    private Status status;

    @XmlElement(nillable = true, required = true)
    private Date time;

    @XmlElement(required = true)
    private String orgId;

    @XmlElement(required = true)
    private String source;

    @XmlElement(required = true)
    private String client;

    @XmlElement(required = true)
    private List<T> data;

    public Event() {
        this.corrId = UUID.randomUUID().toString();
        this.time = new Date();
        this.data = new ArrayList<>();
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Event(String orgId, String source, String verb, Status status) {
        this.orgId = orgId;
        this.source = source;

        this.corrId = UUID.randomUUID().toString();
        this.verb = verb;
        this.status = status;
        this.time = new Date();
        this.data = new ArrayList<>();
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getCorrId() {
        return corrId;
    }

    public void setCorrId(String corrId) {
        this.corrId = corrId;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public void addData(T data) {
        this.data.add(data);
    }

    @Override
    public String toString() {
        return "Event{" +
                "corrId='" + corrId + '\'' +
                ", verb='" + verb + '\'' +
                ", status=" + status +
                ", time=" + time +
                ", orgId='" + orgId + '\'' +
                ", data=" + data +
                '}';
    }
}