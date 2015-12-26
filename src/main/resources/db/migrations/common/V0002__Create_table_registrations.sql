/* 
 * Copyright 2015 EuregJUG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

create table registrations (
    id                 serial primary key,
    event_id           integer not null,
    email              varchar(1024) not null,    
    name               varchar(512) not null,
    first_name         varchar(512),
    CONSTRAINT registrations_events_fk FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT registrations_uk UNIQUE (event_id, email)
);
