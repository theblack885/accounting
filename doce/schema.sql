create table public.accounts (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null default auth.uid() references auth.users(id) on delete cascade,
  name text not null,
  type text not null check (type in ('ASSET','LIABILITY','INCOME','EXPENSE','CAPITAL')),
  is_party boolean not null default false,
  opening numeric(14,2) not null default 0,
  created_at timestamptz not null default now()
);

create table public.vouchers (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null default auth.uid() references auth.users(id) on delete cascade,
  date date not null default current_date,
  type text not null check (type in ('Receipt','Payment','Sales','Purchase','Journal','Contra')),
  debit_id uuid not null references public.accounts(id) on delete restrict,
  credit_id uuid not null references public.accounts(id) on delete restrict,
  amount numeric(14,2) not null check (amount > 0),
  narration text not null default '',
  created_at timestamptz not null default now(),
  check (debit_id <> credit_id)
);

create index on public.accounts (user_id);
create index on public.vouchers (user_id, date desc);

alter table public.accounts enable row level security;
alter table public.vouchers enable row level security;

create policy "own accounts" on public.accounts
  for all using (user_id = auth.uid()) with check (user_id = auth.uid());

create policy "own vouchers" on public.vouchers
  for all using (user_id = auth.uid()) with check (user_id = auth.uid());
