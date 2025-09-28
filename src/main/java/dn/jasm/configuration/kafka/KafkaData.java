package dn.jasm.configuration.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
public class KafkaData {

    private String timeStamp;
    private TopicName topicName;
    private long senderId;

    public KafkaData(String timeStamp,
                     TopicName topicName,
                     long senderId) {
        this.timeStamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-Mm-Dd"));
        this.topicName = topicName;
        this.senderId = senderId;
    }
}
