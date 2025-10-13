variable "db_url" {
  type    = string
  default = "postgres://realworld:realworld@localhost:5432/realworld?sslmode=disable"
}

env "local" {
  src = "file://db/schema.hcl"
  url = var.db_url
  dev = "docker://postgres/15/dev"
  migration {
    dir = "file://db/migrations"
  }
  format {
    migrate {
      diff = "{{ sql . \"  \" }}"
    }
  }
}

env "test" {
  src = "file://db/schema.hcl"
  url = "postgres://realworld:realworld@localhost:5433/realworld_test?sslmode=disable"
  dev = "docker://postgres/15/test"
  migration {
    dir = "file://db/migrations"
  }
  format {
    migrate {
      diff = "{{ sql . \"  \" }}"
    }
  }
}
