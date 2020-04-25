package az.gdg.msauth.service.impl;

import az.gdg.msauth.model.dto.MailDTO;
import az.gdg.msauth.service.EmailService;
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
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final Source source;

    @Override
    public void sendToQueue(MailDTO mailDTO) {
        source.output().send(MessageBuilder.withPayload(mailDTO).build());
    }

    @Override
    public void sendEmail(String url, String email, String token, String subject, String option) {
        logger.info("Action.sendEmail.start : email {}", email);
        MailDTO mail = new MailDTO().builder()
                .to(Collections.singletonList(email))
                .subject(subject)
                .body("<h2>" + option + "</h2>" + "</br>" +
                        "<a href=" +
                        url + "?token=" + token + ">" +
                        url + "?token=" + token + "</a>")
                .build();

        sendToQueue(mail);

        logger.info("Action.sendEmail.stop.success : email {}", email);
    }
}
