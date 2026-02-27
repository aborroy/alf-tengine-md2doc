# syntax=docker/dockerfile:1.4
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests

FROM eclipse-temurin:17-jre

# --- Install Pandoc and LaTeX for PDF generation ---
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
        pandoc \
        texlive-latex-base \
        texlive-latex-recommended \
        texlive-latex-extra \
        texlive-fonts-recommended \
        texlive-xetex \
        lmodern \
        curl \
    && rm -rf /var/lib/apt/lists/*

# --- Add non-root user ---
ARG GROUP_NAME=alfresco
ARG GROUP_ID=1001
ARG USER_NAME=alfmd2doc
ARG USER_ID=33034
ARG HOME_DIR=/home/${USER_NAME}

RUN groupadd -g ${GROUP_ID} ${GROUP_NAME} && \
    useradd -u ${USER_ID} -g ${GROUP_NAME} -m -d ${HOME_DIR} ${USER_NAME}

ENV HOME=${HOME_DIR}

WORKDIR /app
COPY --from=build /workspace/target/*.jar /app/app.jar

# Reference template for styled DOCX output
COPY reference.docx /app/reference.docx

RUN chown -R ${USER_NAME}:${GROUP_NAME} /app ${HOME_DIR}

USER ${USER_NAME}

EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
