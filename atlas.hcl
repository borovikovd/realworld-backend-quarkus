variable "db_url" {
  type    = string
  default = "postgres://realworld:realworld@localhost:5432/realworld?sslmode=disable"
}

env "local" {
  src = "file://db/schema.hcl"
  url = var.db_url
  dev = "docker://postgres/18/dev"
  migration {
    dir = "file://db/migrations"
  }
  format {
    migrate {
      diff = "{{ sql . \"  \" }}"
    }
  }
}
