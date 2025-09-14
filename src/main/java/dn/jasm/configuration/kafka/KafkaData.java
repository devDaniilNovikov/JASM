package dn.jasm.configuration.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class KafkaData {

    private LocalDate timeStamp;
    private TopicName topicName;
    private long senderId;
}
