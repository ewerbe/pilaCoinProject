package br.com.ufsm.csi.pilacoin.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PilaValidationService {

    @Value("clients-msgs")
    private String queue_msgs;

    @Value("clients-errors")
    private String queue_errors; //tratar se vierem erros

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = {"${queue.dificuldade}"})
    public void receivePilaCoin(@Payload String strPilaCoinJson) {
        System.out.println("Dificuldade: " + strPilaCoinJson);
        //pegar a dificuldade e passar para o PilaCoin usando objectmapper do jackson;
    }

//    @RabbitListener(queues = {"{queue.valida_pila}"})
//    public String validaPilaMinerado()
}
