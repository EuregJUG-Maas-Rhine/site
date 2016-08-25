insert into events(held_on, name, description) values(now(), 'test', 'test');
insert into registrations(event_id, email, name) select max(id), 'test@test.com', 'test' from events;