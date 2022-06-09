// Copyright 2022 jefrajames
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.jefrajames.lrademo.holiday.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name="compensented_lra", indexes = @Index(columnList = "lra_id"))
@NoArgsConstructor
@ToString
public class CompensatedLRA extends PanacheEntity {

    @Column(name="compensated_at")
    public LocalDateTime compensatedAt;

    @Column(name="lra_id", unique = true)
    @JsonProperty("lra_id")
    public String lraId;

    public CompensatedLRA(String lraId) {
        this.lraId = lraId;
        this.compensatedAt = LocalDateTime.now();
    }

}
