package com.chenchen.event.dto;

import lombok.Data;

@Data
public class EventPublishDTO {

  private String namespace;

  private String event;

  private String message;

}
