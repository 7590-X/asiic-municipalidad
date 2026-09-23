package com.assic.muni.application.cqrs.cmd;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetMisIncidenciasQuery {
    private String userId;
}
