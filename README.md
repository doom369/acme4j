# ACME Java Client ![build status](https://shredzone.org/badge/acme4j.svg) ![maven central](https://shredzone.org/maven-central/org.shredzone.acme4j/acme4j/badge.svg)

This is a Java client for the _Automatic Certificate Management Environment_ (ACME) protocol as specified in [RFC 8555](https://tools.ietf.org/html/rfc8555).

ACME is a protocol that a certificate authority (CA) and an applicant can use to automate the process of verification and certificate issuance.

This Java client helps connecting to an ACME server, and performing all necessary steps to manage certificates.

It is an independent open source implementation that is not affiliated with or endorsed by _Let's Encrypt_.

## Features

* Fully [RFC 8555](https://tools.ietf.org/html/rfc8555) compliant
* Supports the `http-01`, `dns-01` and `tls-alpn-01` challenges
* Supports the [acme-ip draft](https://tools.ietf.org/html/draft-ietf-acme-ip)
* Supports the [acme-star draft](https://tools.ietf.org/html/draft-ietf-acme-star) for short-term automatic certificate renewal (experimental)
* Easy to use Java API
* Requires JRE 8 (update 101) or higher
* Built with maven, packages available at [Maven Central](http://search.maven.org/#search|ga|1|g%3A%22org.shredzone.acme4j%22)
* Requires [jose4j](https://bitbucket.org/b_c/jose4j/wiki/Home), [Bouncy Castle](https://www.bouncycastle.org/) and [slf4j](http://www.slf4j.org/) as dependencies. If you have other means of generating key pairs and CSRs, you can even do without `acme4j-utils` and Bouncy Castle as dependency.
* Extensive unit and integration tests

## Usage

* See the [online documentation](https://shredzone.org/maven/acme4j/) about how to use _acme4j_.
* For a quick start, have a look at [the source code of an example](https://github.com/shred/acme4j/blob/master/acme4j-example/src/main/java/org/shredzone/acme4j/ClientTest.java).

## Contribute

* Fork the [Source code at GitHub](https://github.com/shred/acme4j). Feel free to send pull requests.
* Found a bug? [File a bug report!](https://github.com/shred/acme4j/issues)

## License

_acme4j_ is open source software. The source code is distributed under the terms of [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## Acknowledgements

* I would like to thank Brian Campbell and all the other [jose4j](https://bitbucket.org/b_c/jose4j/wiki/Home) developers. _acme4j_ would not exist without your excellent work.
* Thanks to [Daniel McCarney](https://github.com/cpu) for his help with the ACME protocol, Pebble, and Boulder.
* [Ulrich Krause](https://github.com/eknori) for his help to make _acme4j_ run on IBM Java VMs.
* I also like to thank [everyone who contributed to _acme4j_](https://github.com/shred/acme4j/graphs/contributors).
* [JetBrains](https://www.jetbrains.com/?from=acme4j) kindly provided a free open source license for IntelliJ IDEA.
