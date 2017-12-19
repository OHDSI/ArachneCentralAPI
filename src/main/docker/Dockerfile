FROM hub.arachnenetwork.com/portal_env:1.0.2
MAINTAINER alexandr.ryabokon@odysseusinc.com
USER root
VOLUME /tmp
ADD portal-exec.jar /portal.jar
RUN apt-get update && apt-get -y -q install libreoffice\
    libreoffice-writer ure libreoffice-java-common libreoffice-core\
    libreoffice-common openjdk-8-jre fonts-opensymbol hyphen-fr hyphen-de\
    hyphen-en-us hyphen-it hyphen-ru fonts-dejavu fonts-dejavu-core fonts-dejavu-extra\
    fonts-noto fonts-dustin fonts-f500 fonts-fanwood fonts-freefont-ttf fonts-liberation\
    fonts-lmodern fonts-lyx fonts-sil-gentium fonts-texgyre fonts-tlwg-purisa && apt-get -q -y remove libreoffice-gnome
ADD run.sh /bin/run.sh
RUN chmod +x /bin/run.sh
EXPOSE 8080 5434 8983
CMD ["/bin/run.sh"]