FROM clojure:openjdk-8-boot-2.8.2

RUN apt-get update && apt-get install -y \
    curl \
    maven \
    git \
    unzip \
    wget \
    less \
    vim \
    python

# create the 'spring' user
RUN groupadd spring && \
    useradd --create-home --shell /bin/bash --no-log-init -g spring spring

USER spring:spring

# install the CRAFT distribution then run boot once to initialize it,
# then once more to download the dependencies for the CRAFT project
ENV CRAFT_VERSION='4.0.1'

RUN cd /home/spring && \
    wget https://github.com/UCDenver-ccp/CRAFT/archive/v${CRAFT_VERSION}.tar.gz && \
    tar -xzf v${CRAFT_VERSION}.tar.gz && \
    rm v${CRAFT_VERSION}.tar.gz && \
    cd /home/spring/CRAFT-${CRAFT_VERSION} && \
    boot -h && \
    boot dependency -h

# build annotation files required for evaluations
RUN cd /home/spring/CRAFT-${CRAFT_VERSION} && \
    boot concept -t CHEBI convert -b && \
    boot concept -t CHEBI -x convert -b && \
    boot concept -t CL convert -b && \
    boot concept -t CL -x convert -b && \
    boot concept -t GO_BP convert -b && \
    boot concept -t GO_BP -x convert -b && \
    boot concept -t GO_CC convert -b && \
    boot concept -t GO_CC -x convert -b && \
    boot concept -t GO_MF convert -b && \
    boot concept -t GO_MF -x convert -b && \
    boot concept -t MOP convert -b && \
    boot concept -t MOP -x convert -b && \
    boot concept -t NCBITaxon convert -b && \
    boot concept -t NCBITaxon -x convert -b && \
    boot concept -t PR convert -b && \
    boot concept -t PR -x convert -b && \
    boot concept -t SO convert -b && \
    boot concept -t SO -x convert -b && \
    boot concept -t UBERON convert -b && \
    boot concept -t UBERON -x convert -b

ENV EVAL_SERVICE_VERSION='0.1.0'
ENV MAIN_OPTS=''
ENV JAVA_OPTS='-Xmx5g'
RUN mkdir -p /home/spring/nlp-eval-service
COPY target/nlp-eval-service-${EVAL_SERVICE_VERSION}.jar /home/spring/nlp-eval-service
WORKDIR /home/spring/nlp-eval-service
ENTRYPOINT java $JAVA_OPTS -jar ./nlp-eval-service-${EVAL_SERVICE_VERSION}.jar $MAIN_OPTS
EXPOSE 8080

