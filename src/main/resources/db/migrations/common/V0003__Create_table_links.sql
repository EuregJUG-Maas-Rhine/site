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

create table links (
    id                    serial primary key,
    type                  varchar(32) not null default 'generic',    
    target                varchar(1024) not null,    
    title                 varchar(512) not null,
    sort_col             integer not null default 0,
    icon                  varchar(128),
    local_image_resource  varchar(128),    
    
    CONSTRAINT links_uk UNIQUE (target)
);

insert into links(type, target, title) values('generic', 'http://www.jugevents.org/jugevents/', 'JUG Events');
insert into links(type, target, title) values('generic', 'https://www.java.net/jugs/java-user-groups', 'Java User Groups');
insert into links(type, target, title) values('generic', 'http://www.ijug.eu/', 'iJUG e.V.');

insert into links(type, target, title, icon, sort_col) values('profile', 'https://www.twitter.com/euregjug', 'Twitter', 'fa-twitter', 10);
insert into links(type, target, title, icon, sort_col) values('profile', 'https://github.com/EuregJUG-Maas-Rhine', 'GitHub', 'fa-github', 20);

insert into links(type, target, title, local_image_resource, sort_col) values('sponsor', 'http://www.enerko-informatik.de', 'ENERKO Informatik GmbH', 'logo-enerko.png', 10);
insert into links(type, target, title, local_image_resource, sort_col) values('sponsor', 'http://www.oreilly.de', 'O''Reilly', 'logo-oreilly.jpg', 20);
insert into links(type, target, title, local_image_resource, sort_col) values('sponsor', 'http://www.bitstars.com', 'bitstars', 'logo-bitstars.png', 30);
insert into links(type, target, title, local_image_resource, sort_col) values('sponsor', 'http://www.ijug.eu', 'iJUG', 'logo-ijug.jpg', 40);

commit;