package io.tiago.monitor.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.Objects;

public class Node implements Serializable {

    private String key;

    private int pollFrequency;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime start;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime end;

    private long expire;

    private String host;

    private int port;

    private boolean up;

    public Node() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getPollFrequency() {
        return pollFrequency;
    }

    public void setPollFrequency(int pollFrequency) {
        this.pollFrequency = pollFrequency;
    }

    public LocalTime getStart() {
        return start;
    }

    public void setStart(LocalTime start) {
        this.start = start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public void setEnd(LocalTime end) {
        this.end = end;
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isUp() {
        return up;
    }

    public void setUp(boolean up) {
        this.up = up;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return pollFrequency == node.pollFrequency &&
                expire == node.expire &&
                port == node.port &&
                up == node.up &&
                Objects.equals(start, node.start) &&
                Objects.equals(end, node.end) &&
                Objects.equals(host, node.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pollFrequency, start, end, expire, host, port, up);
    }

    @Override
    public String toString() {
        return "Node{" +
                "pollFrequency=" + pollFrequency +
                ", start=" + start +
                ", end=" + end +
                ", expire=" + expire +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", up=" + up +
                '}';
    }
}
