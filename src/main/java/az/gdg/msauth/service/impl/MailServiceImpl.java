package az.gdg.msauth.service.impl;

import az.gdg.msauth.model.dto.MailDTO;
import az.gdg.msauth.service.MailService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@EnableBinding(Source.class)
@AllArgsConstructor
public class MailServiceImpl implements MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);
    private final Source source;

    @Override
    public void sendToQueue(MailDTO mailDTO) {
        source.output().send(MessageBuilder.withPayload(mailDTO).build());
    }

    @Override
    public void sendMail(String url, String mail, String subject) {
        logger.info("Action.sendMail.start.mail : {}", mail);
        MailDTO mailDTO = MailDTO.builder()
                .to(Collections.singletonList(mail))
                .subject(subject)
                .body(url)
                .build();

        sendToQueue(mailDTO);

        logger.info("Action.sendEmail.stop.success.mail : {}", mail);
    }
}
