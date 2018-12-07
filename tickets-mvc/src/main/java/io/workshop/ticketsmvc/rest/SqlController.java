package io.workshop.ticketsmvc.rest;

import io.workshop.ticketsmvc.model.RyanairTicketEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Slf4j
@Configuration
@RestController
@RequestMapping("/sql")
public class SqlController {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/jpa")
    public List<RyanairTicketEntity> getJpaEvents() {
        return entityManager
                .createQuery("SELECT rte FROM RyanairTicketEntity rte", RyanairTicketEntity.class)
                .getResultList();
    }

    @Transactional
    @GetMapping("/jpa/add")
    public RyanairTicketEntity addJpaRow() {
        RyanairTicketEntity entity = RyanairTicketEntity.builder()
                .event("JPA event")
                .name("JPA name")
                .date(new Date())
                .city("JPA City")
                .build();
        entityManager.persist(entity);

        return entity;
    }

    @GetMapping("/jdbc")
    public List<RyanairTicketEntity> getJdbcEvents() {
        return jdbcTemplate.query(
                "SELECT * FROM ryanair_tickets",
                new Object[]{},
                (rs, rowNum) -> RyanairTicketEntity.builder()
                        .id(rs.getLong("id"))
                        .event(rs.getString("event"))
                        .name(rs.getString("name"))
                        .date(rs.getDate("date"))
                        .city(rs.getString("city"))
                        .build());
    }

    @GetMapping("jdbc/add")
    public Integer addJdbcRow() {
        return jdbcTemplate
                .update("INSERT INTO `ryanair_tickets` (`event`, `name`, `date`, `city`) VALUES (?, ?, ?, ?)",
                        "Jdbc event", "Jdbc name", "2020-06-06", "Jdbc City");
    }
}
