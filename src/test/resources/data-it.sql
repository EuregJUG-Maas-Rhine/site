insert into events(held_on, name, description) values(now() + interval '3 hour', 'test', 'test');
insert into registrations(event_id, email, name) select max(id), 'test@test.com', 'test' from events;

insert into events(held_on, name, description) values(now() - interval '3 hour', 'test2', 'test2');
insert into registrations(event_id, email, name) select max(id), 'test1@test.com', 'test' from events;
insert into registrations(event_id, email, name) select max(id), 'test2@test.com', 'test' from events;
insert into registrations(event_id, email, name) select max(id), 'test3@test.com', 'test' from events;

insert into events(held_on, name, description, number_of_registrations) values(now() - interval '4 hour', 'test3', 'test3', 23);
