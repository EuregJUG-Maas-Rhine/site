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

create table events (
    id                 serial primary key,
    held_on            timestamp with time zone not null,
    name               varchar(512) not null,
    description        varchar(2048) not null,
    needs_registration boolean not null default false,
    type               varchar(32) not null default 'talk',
    CONSTRAINT events_uk UNIQUE (held_on, name)
);
