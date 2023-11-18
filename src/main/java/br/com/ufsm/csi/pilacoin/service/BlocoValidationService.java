package br.com.ufsm.csi.pilacoin.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BlocoValidationService {

    @Autowired
    private RabbitTemplate rabbitTemplate;
}
