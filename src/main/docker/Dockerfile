FROM hub.odysseusinc.com/portal_env:1.1.0
MAINTAINER alexandr.cumarav@odysseusinc.com
USER root
VOLUME /tmp
ADD portal-exec.jar /portal.jar
ADD run.sh /bin/run.sh
RUN chmod +x /bin/run.sh
EXPOSE 8080 5434 8983
CMD ["/bin/run.sh"]